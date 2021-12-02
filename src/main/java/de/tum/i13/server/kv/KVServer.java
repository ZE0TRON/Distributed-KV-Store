package de.tum.i13.server.kv;

public interface KVServer {

    /**
     * Returns the ranges and which KVStores are responsible for the range.
     *
     * @return the ranges map, which is indexed by the given <ip_addr>:<port> pairs.
     *
     */
    public KVServerMessage keyrange();

    public KVServerMessage processStorageCommand();

    public KVServerMessage rebalanceNodeGiveKeysOut();

    public KVServerMessage rebalanceNodeTakeKeysIn();

    public KVServerMessage shutdownNode();
}
