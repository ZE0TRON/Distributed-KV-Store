package de.tum.i13.server.kv;

public enum EcsConnectionState {
    READY_FOR_CONNECTION,
    WAITING_FOR_INITIALIZATION,
    WAITING_FOR_METADATA,
    READY_FOR_SERVER_DATA_RETRIEVAL,

}
