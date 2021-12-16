package de.tum.i13.server.kv;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.logging.Logger;

public class CommandProcessor implements de.tum.i13.shared.CommandProcessor {


    private CommandProcessor prevKVServer;
    private CommandProcessor nextKVServer;
    private ServerMetadata metadata;

    private String dataRangeStart;
    private String dataRangeEnd;

    private ServerState serverState;
    private EcsConnectionState ecsConnectionState;

    private KVStore kvStore;

    private static final Logger LOGGER = Logger.getLogger(CommandProcessor.class.getName());
    public CommandProcessor(KVStore kvStore) {
        this.kvStore = kvStore;
    }

    @Override
    public String process(String command) {
        KVClientMessage kvClientMessage;
        String[] parts = command.split(" ");
        if (parts.length == 0) {
           parts = new String[]{"help"};
        }
        LOGGER.info("received command " + command);
        try {
            switch (parts[0]) {
                case "put":
                    if (parts.length < 2) {
                        kvClientMessage = kvStore.commandNotFound(command);
                        break;
                    }
                    else if (parts.length == 2) {
                        kvClientMessage = kvStore.put(parts[1], null);
                    }
                    else {
                        String args = command.substring(command.indexOf(' ') + 1);
                        String value = args.substring(args.indexOf(' ') + 1);
                        kvClientMessage = kvStore.put(parts[1], value);
                    }
                    break;
                case "get":
                    if (parts.length < 2) {
                        kvClientMessage = kvStore.commandNotFound(command);
                        break;
                    }
                    kvClientMessage = kvStore.get(parts[1]);
                    break;
                case "delete":
                    if (parts.length < 2) {
                        kvClientMessage = kvStore.commandNotFound(command);
                        break;
                    }
                    kvClientMessage = kvStore.put(parts[1], null);
                    break;
                case "keyrange":
                    if (parts.length != 1){
                        kvClientMessage = kvStore.commandNotFound(command);
                        break;
                    }
                    return this.keyrange();
                default:
                    LOGGER.info("command not found");
                    kvClientMessage = kvStore.commandNotFound(command);
            }
            return kvClientMessage.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

//    @Override
//    public String processEcsCommand(String command){
//        ServerMessage serverMessage = null;
//        String[] parts = command.split(" ");
//        if (parts.length == 0) {
//            parts = new String[]{"help"};
//        }
//        LOGGER.info("received command from ECS: " + command);
//
//        try {
//            switch (parts[0]) {
//                case "add_server":
//
//                    //serverMessage = new ServerMessageImpl(ServerMessage.StatusType.);
//                    break;
//                case "health_check":
//                    serverMessage = new ServerMessageImpl(ServerMessage.StatusType.HEALTHY);
//                    break;
//                default:
//                    serverMessage = new ServerMessageImpl(ServerMessage.StatusType.COMMAND_NOT_FOUND);
//                    LOGGER.info("command not found");
//            }
//            return serverMessage.toString();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

    @Override
    public String connectionAccepted(InetSocketAddress address, InetSocketAddress remoteAddress) {
        //TODO
        LOGGER.fine("Connection accepted address: " + address + " remote address: " + remoteAddress);
        return new KVClientMessageImpl(null, null, KVClientMessage.StatusType.CONNECTION_ESTABLISHED).toString();
    }

    @Override
    public void connectionClosed(InetAddress address) {
        LOGGER.fine("Connection closed address: " + address );
        //TODO
    }

    /**
     * Returns the ranges and which KVStores are responsible for the range.
     *
     * @return the ranges map, which is indexed by the given <ip_addr>:<port> pairs.
     *
     */
    public String keyrange(){
        if (this.serverState.equals(ServerState.SERVER_STOPPED)){
            ServerMessage kvServerResponseMessage = new ServerMessageImpl(ServerMessage.StatusType.SERVER_STOPPED);
            return kvServerResponseMessage.toString();
        }
        else {
            return metadata.toString();
        }
    };

    public String getDataRangeStart() {
        return dataRangeStart;
    }

    public void setDataRangeStart(String dataRangeStart) {
        this.dataRangeStart = dataRangeStart;
    }

    public String getDataRangeEnd() {
        return dataRangeEnd;
    }

    public void setDataRangeEnd(String dataRangeEnd) {
        this.dataRangeEnd = dataRangeEnd;
    }

    public CommandProcessor getPrevKVServer() {
        return prevKVServer;
    }

    public void setPrevKVServer(CommandProcessor prevKVServer) {
        this.prevKVServer = prevKVServer;
    }

    public CommandProcessor getNextKVServer() {
        return nextKVServer;
    }

    public void setNextKVServer(CommandProcessor nextKVServer) {
        this.nextKVServer = nextKVServer;
    }

    public ServerState getServerState() {
        return serverState;
    }

    public void setServerState(ServerState serverState) {
        this.serverState = serverState;
    }

    public EcsConnectionState getEcsConnectionState() {
        return ecsConnectionState;
    }

    public void setEcsConnectionState(EcsConnectionState ecsConnectionState) {
        this.ecsConnectionState = ecsConnectionState;
    }

    public void processStorageCommand(){

    };

    public void rebalanceNodeGiveKeysOut(){

    };

    public void rebalanceNodeTakeKeysIn(){

    };

    public void shutdownNode(){

    };
}
