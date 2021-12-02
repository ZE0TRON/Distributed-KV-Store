package de.tum.i13.server.kv;

import de.tum.i13.server.storageManagment.CacheManager;

public class KVServerImpl {
    private KVStore kvStore;

    private KVServer prevKVServer;
    private KVServer nextKVServer;

    private KVServerMetadata metadata;

    private String dataRangeStart;
    private String dataRangeEnd;

    private KVServerState state;

    public KVServerImpl(KVStore kvStore) {
        this.kvStore = kvStore;
    }


}
