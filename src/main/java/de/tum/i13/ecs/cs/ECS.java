package de.tum.i13.ecs.cs;

import com.sun.tools.javac.util.Pair;
import de.tum.i13.shared.ConnectionManager.ClientConnectionThread;
import de.tum.i13.shared.Server;
import de.tum.i13.shared.keyring.*;
import de.tum.i13.shared.ConnectionManager.ConnectionManagerInterface;
import de.tum.i13.shared.Constants;

import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;

public class ECS implements ConfigurationService {

    private static ECS instance;
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
       metadata = keyRingService.serializeKeyRing();
    }

    @Override
    public void updateMedata() {
        createNewMetaData();
        // TODO send metadata to all servers
    }

    @Override
    public void handoverFinished(Pair<String, String> keyRange){
        if (onGoingRebalance.getKeyRange() != keyRange) {
            // TODO log here
            return;
            //throw new Exception("This rebalance is not in progress how can it be finished ??");
        }
        onGoingRebalance = null;
        updateMedata();
        if (!rebalanceQueue.isEmpty()) {
            startHandoverProcess(this.rebalanceQueue.poll());
        }
    }



    private void startHandoverProcess(RebalanceOperation rebalanceOperation) {
        if(onGoingRebalance != null) {
            // TODO log here
            return;
            // throw new Exception("Another re-balance operation is going on");
        }
        onGoingRebalance = rebalanceOperation;
        if (onGoingRebalance.getRebalanceType() == RebalanceType.ADD) {
          Server server = rebalanceOperation.getReceiverServer();
          ConnectionManagerInterface connectionManager = ClientConnectionThread.connections.get(server.toHashableString());
          if(keyRingService.isKeyringEmpty()) {
              connectionManager.send("first_key_range");
          } else {
              connectionManager.send("init_key_range " +
                      rebalanceOperation.getKeyRange().fst + " " +
                      rebalanceOperation.getKeyRange().snd + " " +
                      rebalanceOperation.getSenderServer());
          }
        }
        else if (onGoingRebalance.getRebalanceType() == RebalanceType.DELETE) {

        }

    }

    private void queueHandoverProcess(RebalanceOperation rebalanceOperation) {
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
