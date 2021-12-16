package de.tum.i13.server.kv;

import java.util.Locale;

public class ServerMessageImpl implements ServerMessage {
    private final StatusType status;

    public ServerMessageImpl(StatusType status) {
        this.status = status;
    }

    @Override
    public ServerMessage.StatusType getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return status.toString().toLowerCase(Locale.ROOT);
    }

}
