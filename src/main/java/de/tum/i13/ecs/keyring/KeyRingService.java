package de.tum.i13.ecs.keyring;


import de.tum.i13.shared.Constants;
import de.tum.i13.shared.Server;
import de.tum.i13.shared.BST.BST;
import de.tum.i13.shared.BST.RingNode;

import java.lang.reflect.Array;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class KeyRingService {

    private static KeyRingService instance;
    private final BST keyRing;
    private KeyRingService() {
        keyRing = new BST();
    }

    private KeyRingService(BST keyRing) {
        this.keyRing = keyRing;
    }

    public static KeyRingService getInstance(){
        if(instance == null) {
            instance = new KeyRingService();
        }
        return instance;
    }
    public static KeyRingService getInstance(String metadata){
        if(instance == null) {
            instance = KeyRingService.keyRingServiceFromMetadata(metadata);
        }
        return instance;
    }

    public void put(RingItem ringItem) {
       keyRing.insert(RingNode.fromRingItem(ringItem)) ;
    }

    public RingItem get(String key) {
        return RingItem.fromRingNode(keyRing.search(key));
    }

    public RingItem findPredecessor(String key) {
        return RingItem.fromRingNode(keyRing.predecessorOfKey(key));
    }

    public RingItem findSuccessor(String key) {
        return RingItem.fromRingNode(keyRing.successorOfKey(key));
    }

    public void delete(RingItem ringItem) {
        keyRing.delete(RingNode.fromRingItem(ringItem));
    }
    private static KeyRingService keyRingServiceFromMetadata(String metadata) {
        BST bst = BST.deserialize(metadata);
        return new KeyRingService(bst);
    }
    public String serializeKeyRing() {
        return keyRing.serialize();
    }

    public boolean isKeyringEmpty() {
        return this.keyRing.getRoot() == null;
    }

    public Server getServerForStorageKey(String key) {
        try {
            ConsistentHashingService chs = new ConsistentHashingService(Constants.HASHING_ALGORITHM);
            String hashedKey = ConsistentHashingService.byteHashToStringHash(chs.hash(key));
            return this.findPredecessor(hashedKey).value;
        }
        catch (NoSuchAlgorithmException e) {
           return null;
        }
    }

    public String serializeKeyRanges(boolean includeReplicas) {
       return keyRing.serializeKeyRanges(includeReplicas);
    }

    public int getCount() {
        return this.keyRing.nodeCount();
    }

    public ArrayList<RingNode> getAllItems() {
       return this.keyRing.dfs();
    }

}
