package de.tum.i13.server.kv;

public interface ServerMessage {
    enum StatusType {

        HEALTHY,
        COMMAND_NOT_FOUND
    }

    StatusType getStatus();
}

