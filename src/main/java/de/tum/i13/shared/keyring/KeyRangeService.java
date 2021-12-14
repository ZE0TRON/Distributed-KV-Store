package de.tum.i13.shared.keyring;


/*
    Scenario 1: new server joined the Cluster
    - Create the hash of ip,port
    - put the hash in the keyring
    - with key as hash and server info as value


 */
public interface KeyRangeService {
    void put(RingItem ringItem);
    RingItem get(String key);
    RingItem findPredecessor(String key);
    void delete(RingItem ringItem);
    RingItem findSuccessor(String key);
}
