package de.tum.i13.server.kv;

public interface ServerMessage {
    public enum StatusType {
        SERVER_STOPPED,
        SERVER_NOT_RESPONSIBLE,
        SERVER_WRITE_LOCK,
        KEYRANGE_SUCCESS,
        HEALTHY,
        COMMAND_NOT_FOUND
    }

    public StatusType getStatus();
}

