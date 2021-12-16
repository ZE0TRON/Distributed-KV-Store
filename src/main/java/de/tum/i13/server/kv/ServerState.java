package de.tum.i13.server.kv;

public enum ServerState {
    SERVER_STOPPED,
    SERVER_NOT_RESPONSIBLE,
    SERVER_WRITE_LOCK,
    RUNNING
}
