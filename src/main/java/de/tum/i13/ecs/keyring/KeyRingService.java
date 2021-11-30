package de.tum.i13.ecs.keyring;


import de.tum.i13.ecs.BST.BST;
import de.tum.i13.ecs.BST.RingNode;

import java.security.Key;

public class KeyRingService implements KeyRangeService{

    private static KeyRingService instance;
    private final BST keyRing;
    private KeyRingService() {
        keyRing = new BST();
    }

    public static KeyRingService getInstance(){
        if(instance == null) {
            instance = new KeyRingService();
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
        return RingItem.fromRingNode(keyRing.successorOfKey(key));
    }

    @Override
    public void delete(RingItem ringItem) {
        keyRing.delete(RingNode.fromRingItem(ringItem));
    }
}
