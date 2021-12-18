package de.tum.i13.server.kv;

public interface ServerMessage {
    public enum StatusType {

        HEALTHY,
        COMMAND_NOT_FOUND
    }

    public StatusType getStatus();
}

