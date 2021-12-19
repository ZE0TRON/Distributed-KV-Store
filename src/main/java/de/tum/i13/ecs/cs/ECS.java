package de.tum.i13.ecs.cs;

import de.tum.i13.shared.Pair;
import de.tum.i13.shared.ConnectionManager.ServerConnectionThread;
import de.tum.i13.shared.Server;
import de.tum.i13.shared.keyring.*;
import de.tum.i13.shared.ConnectionManager.ConnectionManagerInterface;
import de.tum.i13.shared.Constants;

import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.logging.Logger;

public class ECS implements ConfigurationService {

    private static ECS instance;
    private static final Logger LOGGER = Logger.getLogger(ECS.class.getName());
    private final KeyRingService keyRingService;
    private HashService hashService;

    private LinkedList<RebalanceOperation> rebalanceQueue;
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
    }

    public static ECS getInstance() {
       if (instance == null)  {
           instance = new ECS();
       }
       return instance;
    }


    @Override
    public void addServer(Server server) {
        RingItem newRingItem = RingItem.createRingItemFromServer(server);
        keyRingService.put(newRingItem);
        Server receiverServer = server;
        RingItem oldRingItem = keyRingService.findPredecessor(Server.serverToHashString(server));
        Server senderServer = oldRingItem.value;
        Pair<String, String> keyRange = new Pair<>(oldRingItem.key, newRingItem.key);
        RebalanceOperation rebalanceOperation = new RebalanceOperation(senderServer, receiverServer, keyRange, RebalanceType.ADD);
        queueHandoverProcess(rebalanceOperation);
    }

    @Override
    public void deleteServer(Server server) {
        RingItem ringItemToDelete = keyRingService.get(Server.serverToHashString(server));
        RingItem ringItem = keyRingService.findSuccessor(ringItemToDelete.key);
        keyRingService.delete(ringItemToDelete);
        Server senderServer = ringItemToDelete.value;
        Server receiverServer = ringItem.value;
        Pair<String, String> keyRange = new Pair<>(ringItemToDelete.key, ringItem.key);
        RebalanceOperation rebalanceOperation = new RebalanceOperation(senderServer, receiverServer , keyRange, RebalanceType.DELETE);
        queueHandoverProcess(rebalanceOperation);
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
        for (ConnectionManagerInterface connection : ServerConnectionThread.connections.values()) {
           connection.send("update_metadata " + metadata);
        }
    }

    @Override
    public void handoverFinished(Pair<String, String> keyRange){
        if (onGoingRebalance.getKeyRange() != keyRange) {
            LOGGER.warning("No re-balance operation on going with keyRange: " + keyRange.fst + "-" + keyRange.snd );
            return;
        }
        onGoingRebalance = null;
        updateMetadata();
        if (!rebalanceQueue.isEmpty()) {
            startHandoverProcess(this.rebalanceQueue.poll());
        }
    }



    private synchronized void startHandoverProcess(RebalanceOperation rebalanceOperation) {
        if(onGoingRebalance != null) {
            // TODO log here
            return;
            // throw new Exception("Another re-balance operation is going on");
        }
        onGoingRebalance = rebalanceOperation;
        if (onGoingRebalance.getRebalanceType() == RebalanceType.ADD) {
          Server server = rebalanceOperation.getReceiverServer();
          ConnectionManagerInterface connectionManager = ServerConnectionThread.connections.get(server.toHashableString());
          // Since the first node added before handover process there should be 1 node in first case
          if(keyRingService.getCount() == 1) {
              connectionManager.send("first_key_range");
              handoverFinished(onGoingRebalance.getKeyRange());
          } else {
              connectionManager.send("init_key_range " +
                      rebalanceOperation.getKeyRange().fst + " " +
                      rebalanceOperation.getKeyRange().snd + " " +
                      rebalanceOperation.getSenderServer().toHashableString());
          }
        }
        else if (onGoingRebalance.getRebalanceType() == RebalanceType.DELETE) {
            Server server = rebalanceOperation.getSenderServer();
            ConnectionManagerInterface connectionManager = ServerConnectionThread.connections.get(server.toHashableString());
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
            startHandoverProcess(this.rebalanceQueue.poll());
        }
    }

    @Override
    public Server getServerForStorageKey(String key) {
        String hashedKey = new String(hashService.hash(key), Constants.STRING_CHARSET);
        return keyRingService.findPredecessor(hashedKey).value;
    }
}
