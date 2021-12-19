package de.tum.i13.server.kv;

import java.util.logging.Logger;

public class CommandProcessor implements de.tum.i13.shared.CommandProcessor {
    private ServerState serverState;
    private EcsConnectionState ecsConnectionState;
    private final KVStore kvStore;

    private static final Logger LOGGER = Logger.getLogger(CommandProcessor.class.getName());
    public CommandProcessor(KVStore kvStore) {
        this.kvStore = kvStore;
    }

    /**
     * Returns the ranges and which KVStores are responsible for the range.
     *
     * @return the ranges map, which is indexed by the given <ip_addr>:<port> pairs.
     *
     */
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
                    if (this.serverState.equals(ServerState.SERVER_STOPPED)){
                        kvClientMessage = new KVClientMessageImpl(null,null, KVClientMessage.StatusType.SERVER_STOPPED);
                    } else {
                        kvClientMessage = new KVClientMessageImpl(KVStoreImpl.metaDataString,null, KVClientMessage.StatusType.KEYRANGE_SUCCESS);
                    }
                    break;
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

    public void setServerState(ServerState serverState) {
        this.serverState = serverState;
    }

    public void setEcsConnectionState(EcsConnectionState ecsConnectionState) {
        this.ecsConnectionState = ecsConnectionState;
    }
}
