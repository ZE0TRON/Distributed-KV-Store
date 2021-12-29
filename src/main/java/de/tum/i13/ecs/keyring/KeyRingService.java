package de.tum.i13.ecs.keyring;


import de.tum.i13.shared.Constants;
import de.tum.i13.shared.Server;
import de.tum.i13.shared.BST.BST;
import de.tum.i13.shared.BST.RingNode;

import java.security.NoSuchAlgorithmException;

public class KeyRingService implements KeyRangeService{

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

    @Override
    public void put(RingItem ringItem) {
       keyRing.insert(RingNode.fromRingItem(ringItem)) ;
    }

    @Override
    public RingItem get(String key) {
        return RingItem.fromRingNode(keyRing.search(key));
    }

    @Override
    public RingItem findPredecessor(String key) {
        return RingItem.fromRingNode(keyRing.predecessorOfKey(key));
    }
    // TODO correct this
    @Override
    public RingItem findSuccessor(String key) {
        return RingItem.fromRingNode(keyRing.successorOfKey(key));
    }

    @Override
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

    public String serializeKeyRanges() {
       return keyRing.serializeKeyRanges();
    }

    public int getCount() {
        return this.keyRing.nodeCount();
    }
}
