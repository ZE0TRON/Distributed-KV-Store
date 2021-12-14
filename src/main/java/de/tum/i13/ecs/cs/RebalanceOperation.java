package de.tum.i13.ecs.cs;

import com.sun.tools.javac.util.Pair;
import de.tum.i13.shared.Server;

public class RebalanceOperation {
    private Server senderServer;
    private Server receiverServer;
    private Pair<String, String> keyRange;
    private RebalanceType rebalanceType;

    public RebalanceOperation(Server senderServer, Server receiverServer, Pair<String, String> keyRange, RebalanceType rebalanceType) {
        this.senderServer = senderServer;
        this.receiverServer = receiverServer;
        this.keyRange = keyRange;
        this.rebalanceType = rebalanceType;
    }

    public RebalanceType getRebalanceType() {
        return rebalanceType;
    }

    public void setRebalanceType(RebalanceType rebalanceType) {
        this.rebalanceType = rebalanceType;
    }

    public Pair<String, String> getKeyRange() {
        return keyRange;
    }

    public void setKeyRange(Pair<String, String> keyRange) {
        this.keyRange = keyRange;
    }

    public Server getReceiverServer() {
        return receiverServer;
    }

    public void setReceiverServer(Server receiverServer) {
        this.receiverServer = receiverServer;
    }

    public Server getSenderServer() {
        return senderServer;
    }

    public void setSenderServer(Server senderServer) {
        this.senderServer = senderServer;
    }
}
