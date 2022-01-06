package de.tum.i13.ecs.cs;

import de.tum.i13.ecs.ECSHeartBeatThread;
import de.tum.i13.shared.Pair;
import de.tum.i13.shared.ConnectionManager.ServerConnectionThread;
import de.tum.i13.shared.Server;
import de.tum.i13.ecs.keyring.*;
import de.tum.i13.shared.ConnectionManager.ConnectionManagerInterface;
import de.tum.i13.shared.Constants;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.logging.Logger;

public class ECS implements ConfigurationService {

    private static ECS instance;
    private static final Logger LOGGER = Logger.getLogger(ECS.class.getName());
    private static int handoverCounter = 0;
    private final KeyRingService keyRingService;
    private HashService hashService;

    private LinkedList<RebalanceOperation> rebalanceQueue;
    private ArrayList<Thread> heartbeatThreads;
    private RebalanceOperation onGoingRebalance;
    private String metadata;

    // TODO lock the correct functions like delete or insert in BST
    private ECS() {
        this.keyRingService = KeyRingService.getInstance();
        try {
            this.hashService = new ConsistentHashingService(Constants.HASHING_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            // TODO Log here
            e.printStackTrace();
        }
        this.rebalanceQueue = new LinkedList<>();
        this.heartbeatThreads = new ArrayList<>();
    }

    public static ECS getInstance() {
       if (instance == null)  {
           instance = new ECS();
       }
       return instance;
    }

    private static boolean getReplicaMod() {
        return ServerConnectionThread.connections.size() >= 3;
    }


    @Override
    public synchronized void addServer(Server server) {
        RingItem newRingItem = RingItem.createRingItemFromServer(server);
        keyRingService.put(newRingItem);
        Server receiverServer = server;
        RingItem oldRingItem = keyRingService.findPredecessor(Server.serverToHashString(server));
        LOGGER.info("Set hashed key: " + newRingItem.key + " for server " +server.getAddress() + " " + server.getPort());
        RingItem successorItem = keyRingService.findSuccessor(Server.serverToHashString(server));
        Server senderServer = successorItem.value;
        Pair<String, String> keyRange = new Pair<>(oldRingItem.key, newRingItem.key);
        RebalanceOperation rebalanceOperation = new RebalanceOperation(senderServer, receiverServer, keyRange, RebalanceType.ADD);
        try {
            Thread heartbeatThread = new ECSHeartBeatThread(server);
            heartbeatThread.start();
            heartbeatThreads.add(heartbeatThread);
            queueHandoverProcess(rebalanceOperation);
        } catch (IOException e) {
            LOGGER.warning("Couldnt connect to the heartbeat port for server: " + server.toHashableString() + " error " + e.getMessage());
            ServerConnectionThread.connections.remove(server.toHashableString());
            return;
        }

        LOGGER.info("Rebalance operation "+ rebalanceOperation +" for add server queued");
    }

    @Override
    public synchronized void deleteServer(Server server) {
        if (getReplicaMod()) {
            serverCrashed(server);
            return;
        }
        RingItem ringItemToDelete = keyRingService.get(Server.serverToHashString(server));
        RingItem ringItem = keyRingService.findSuccessor(ringItemToDelete.key);
        RingItem predecessorItem = keyRingService.findPredecessor(ringItemToDelete.key);
        keyRingService.delete(ringItemToDelete);
        Server senderServer = ringItemToDelete.value;
        Server receiverServer = ringItem.value;
        Pair<String, String> keyRange = new Pair<>(predecessorItem.key, ringItemToDelete.key);
        RebalanceOperation rebalanceOperation = new RebalanceOperation(senderServer, receiverServer , keyRange, RebalanceType.DELETE);
        queueHandoverProcess(rebalanceOperation);
        LOGGER.info("Rebalance operation "+ rebalanceOperation +" for delete server queued");
    }

    @Override
    public Server getServer(String key) {
        return keyRingService.get(key).value;
    }


    private void createNewMetaData() {
        if(keyRingService.isKeyringEmpty()) {
            metadata = "";
        }
        else {
            metadata = keyRingService.serializeKeyRanges();
        }
    }

    @Override
    public void updateMetadata() {
        createNewMetaData();
        LOGGER.info("New metadata created: " + metadata);
        for (ConnectionManagerInterface connection : ServerConnectionThread.connections.values()) {
           connection.send("update_metadata " + metadata);
        }
        LOGGER.info("Metadata sent to all servers");
    }

    @Override
    public boolean handoverFinished(Pair<String, String> keyRange){
        if (!onGoingRebalance.getKeyRange().fst.equals(keyRange.fst) || !onGoingRebalance.getKeyRange().snd.equals(keyRange.snd)) {
            LOGGER.warning("No re-balance operation on going with keyRange: " + keyRange.fst + "-" + keyRange.snd );
            return false;
        }

        LOGGER.info("Finishing handover process with keyrange: " + keyRange.fst + "-" + keyRange.snd);
        boolean endConnection = onGoingRebalance.getRebalanceType() == RebalanceType.DELETE;

        if (onGoingRebalance.getRebalanceType() == RebalanceType.CRASH) {
            handoverCounter--;
            LOGGER.info("Handover count reduced new handover count : " + handoverCounter);
        }

        if (onGoingRebalance.getRebalanceType() != RebalanceType.CRASH || handoverCounter == 0) {
            updateMetadata();
        }

        onGoingRebalance = null;
        if (!rebalanceQueue.isEmpty()) {
            startHandoverProcess(this.rebalanceQueue.poll());
        }
        return endConnection;
    }



    private synchronized void startHandoverProcess(RebalanceOperation rebalanceOperation) {
        LOGGER.info("Starting handover process for rebalance operation" + rebalanceOperation.toString());
        if(onGoingRebalance != null) {
            // TODO log here
            return;
            // throw new Exception("Another re-balance operation is going on");
        }
        onGoingRebalance = rebalanceOperation;
        if (onGoingRebalance.getRebalanceType() == RebalanceType.ADD) {
          Server server = rebalanceOperation.getReceiverServer();
          ConnectionManagerInterface connectionManager = ServerConnectionThread.connections.get(server.toEcsConnectionString());
          // Since the first node added before handover process there should be 1 node in first case
          if(keyRingService.getCount() == 1) {
              connectionManager.send("first_key_range");
              LOGGER.info("Handover first key range");
              handoverFinished(onGoingRebalance.getKeyRange());
          } else {
              LOGGER.info("Handover init key range");
              connectionManager.send("init_key_range " +
                      rebalanceOperation.getKeyRange().fst + " " +
                      rebalanceOperation.getKeyRange().snd + " " +
                      rebalanceOperation.getSenderServer().toHashableString());
          }
        }
        else if (onGoingRebalance.getRebalanceType() == RebalanceType.DELETE || onGoingRebalance.getRebalanceType() == RebalanceType.CRASH) {
            LOGGER.info("Handover delete");
            Server server = rebalanceOperation.getSenderServer();
            ConnectionManagerInterface connectionManager = ServerConnectionThread.connections.get(server.toEcsConnectionString());
            if(keyRingService.isKeyringEmpty()) {
                handoverFinished(onGoingRebalance.getKeyRange());
                return;
            }
            connectionManager.send("handover_start " +
                    rebalanceOperation.getKeyRange().fst + " " +
                    rebalanceOperation.getKeyRange().snd + " " +
                    rebalanceOperation.getReceiverServer().toHashableString());
        }

    }

    private synchronized void queueHandoverProcess(RebalanceOperation rebalanceOperation) {
        rebalanceQueue.addFirst(rebalanceOperation);
        if (onGoingRebalance == null) {
            RebalanceOperation polledOperation = this.rebalanceQueue.poll();
            if(polledOperation != null) {
                startHandoverProcess(polledOperation);
            }
            else {
                LOGGER.warning("Tried starting another handover while the queue is empty");
            }
        }
    }
    // TODO implement
    public synchronized void serverCrashed(Server server) {
        // Starting from predecessor of the crashed server
        // Every server handovers the to the next server for 3 servers
        ServerConnectionThread.connections.remove(server.toHashableString());
        RingItem serverCrashed = keyRingService.get(Server.serverToHashString(server));
        RingItem predecessor =  keyRingService.findPredecessor(Server.serverToHashString(server));
        keyRingService.delete(serverCrashed);
        if (handoverCounter != 0 ) {
            LOGGER.warning("WOWOWOWOW MORE THAN ONE CRASH Ohh boy! handoverCounter: " + handoverCounter);
        }
        handoverCounter += 3;
        for (int i = 0; i < 3; i++) {
            RingItem prePredecessor = keyRingService.findPredecessor(Server.serverToHashString(predecessor.value));
            RingItem prePrePredecessor = keyRingService.findPredecessor(Server.serverToHashString(prePredecessor.value));
            Pair<String, String> keyRange = new Pair<>(prePrePredecessor.key, prePredecessor.key);
            RingItem successor = keyRingService.findSuccessor(Server.serverToHashString(predecessor.value));
            RebalanceOperation rebalanceOperation = new RebalanceOperation(predecessor.value, successor.value, keyRange, RebalanceType.CRASH);
            queueHandoverProcess(rebalanceOperation);
            predecessor = successor;
        }
    }

    @Override
    public Server getServerForStorageKey(String key) {
        String hashedKey = new String(hashService.hash(key), Constants.STRING_CHARSET);
        return keyRingService.findPredecessor(hashedKey).value;
    }
}
