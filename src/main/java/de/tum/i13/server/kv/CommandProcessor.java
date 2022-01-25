package de.tum.i13.server.kv;

import de.tum.i13.client.KeyRange;
import de.tum.i13.shared.Util;
import de.tum.i13.ecs.keyring.ConsistentHashingService;

import java.util.logging.Logger;

public class CommandProcessor implements CommandProcessorInterface {
    public static ServerState serverState;
    private final KVStore kvStore;

    private static final Logger LOGGER = Logger.getLogger(CommandProcessor.class.getName());

    /**
     * Constructs a {@code Command Processor} with a KVStore instance, which handles primitive key-value operations.
     *
     * @param kvStore the beginning of this {@code CommandProcessor}
     */
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
            LOGGER.info("command is empty");
            return kvStore.commandNotFound(command).toString();
        }
        LOGGER.info("Command Processor: Received command " + command);
        LOGGER.info("KVServer state: " + serverState);
        String commandType = parts[0];
        try {
            if (serverState.equals(ServerState.SERVER_STOPPED)){
                return new KVClientMessageImpl(null,null, KVClientMessage.StatusType.SERVER_STOPPED).toString();
            }
            else if (serverState.equals(ServerState.SERVER_WRITE_LOCK) && (parts[0].equals("put") || parts[0].equals("delete"))){
                return new KVClientMessageImpl(null,null, KVClientMessage.StatusType.SERVER_WRITE_LOCK).toString();
            }
            // (ServerState.RUNNING) or (ServerState.SERVER_WRITE_LOCK and command is one of GET, KEYRANGE, KEYRANGE_READ)
            switch (parts[0]) {
                case "put":
                    kvClientMessage = putCommand(command);
                    break;
                case "get":
                    kvClientMessage = getCommand(command);
                    break;
                case "delete":
                    kvClientMessage = deleteCommand(command);
                    break;
                case "keyrange":
                    kvClientMessage = new KVClientMessageImpl(KVStoreImpl.getCoordinatorMetadataString(),null, KVClientMessage.StatusType.KEYRANGE_SUCCESS);
                    break;
                case "keyrange_read":
                    kvClientMessage = new KVClientMessageImpl(KVStoreImpl.getWholeMetadataString(),null, KVClientMessage.StatusType.KEYRANGE_READ_SUCCESS);
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

    private KVClientMessage putCommand(String command) throws Exception {
        String[] parts = command.split(" ");
        KeyRange coordinatorKeyRange = KVStoreImpl.getCoordinatorKeyRange();
        if (parts.length < 2) {
            return kvStore.commandNotFound(command);
        } else if (!Util.isKeyInRange(coordinatorKeyRange.from, coordinatorKeyRange.to, ConsistentHashingService.findHash(parts[1]))) {
            LOGGER.info("KVServer: SERVER_NOT_RESPONSIBLE occurred.");
            return new KVClientMessageImpl( null/*KVStoreImpl.getMetaDataString()*/, null, KVClientMessage.StatusType.SERVER_NOT_RESPONSIBLE);
        }

        if (parts.length == 2) {
            return kvStore.put(parts[1], null, "kvClient");
        }

        String args = command.substring(command.indexOf(' ') + 1);
        String value = args.substring(args.indexOf(' ') + 1);
        return kvStore.put(parts[1], value, "kvClient");
    }

    private KVClientMessage getCommand(String command) throws Exception {
        String[] parts = command.split(" ");
        KeyRange wholeKeyRange = KVStoreImpl.getWholeKeyRange();
        if (parts.length < 2) {
            return kvStore.commandNotFound(command);
        } else if (!Util.isKeyInRange(wholeKeyRange.from, wholeKeyRange.to, ConsistentHashingService.findHash(parts[1]))) {
            LOGGER.info("KVServer: SERVER_NOT_RESPONSIBLE occurred.");
            return new KVClientMessageImpl( null/*KVStoreImpl.getMetaDataString()*/, null, KVClientMessage.StatusType.SERVER_NOT_RESPONSIBLE);
        }
        return kvStore.get(parts[1]);
    }

    private KVClientMessage deleteCommand(String command) throws Exception {
        String[] parts = command.split(" ");
        KeyRange coordinatorKeyRange = KVStoreImpl.getCoordinatorKeyRange();
        if (parts.length < 2) {
            return kvStore.commandNotFound(command);
        } else if (!Util.isKeyInRange(coordinatorKeyRange.from, coordinatorKeyRange.to, ConsistentHashingService.findHash(parts[1]))) {
            LOGGER.info("KVServer: SERVER_NOT_RESPONSIBLE occurred.");
            return new KVClientMessageImpl( null/*KVStoreImpl.getMetaDataString()*/, null, KVClientMessage.StatusType.SERVER_NOT_RESPONSIBLE);
        }
        return kvStore.put(parts[1], null, "kvClient");
    }

    public String processSubscription(String command, String ip) {
        String[] parts = command.split(" ");
        if(parts[0].equals("subscribe")) {
           return subscribeCommand(command, ip).toString();
        } else if(parts[0].equals("unsubscribe")) {
           return unsubscribeCommand(command, ip).toString();
        } else {
            LOGGER.info("command not found");
            return kvStore.commandNotFound(command).toString();
        }
    }

    private KVClientMessage subscribeCommand(String command, String ip) {
        String[] parts = command.split(" ");
        if (parts.length < 3) {
            return kvStore.commandNotFound(command);
        }
        try {
            kvStore.addSubscription(parts[1], ip + ":" + parts[2]);
            return new KVClientMessageImpl(null, null, KVClientMessage.StatusType.SUBSCRIBE_SUCCESS);
        } catch (Exception e) {
            LOGGER.warning("Error during adding subscription " + e.getMessage());
            return new KVClientMessageImpl(null, null, KVClientMessage.StatusType.SUBSCRIBE_ERROR);
        }
    }
    private KVClientMessage unsubscribeCommand(String command, String ip) {
        String[] parts = command.split(" ");
        if (parts.length < 3) {
            return kvStore.commandNotFound(command);
        }
        try {
            kvStore.deleteSubscription(parts[1], ip + ":" + parts[2]);
            return new KVClientMessageImpl(null, null, KVClientMessage.StatusType.UNSUBSCRIBE_SUCCESS);
        } catch (Exception e) {
            LOGGER.warning("Error during deleting subscription " + e.getMessage());
            return new KVClientMessageImpl(null, null, KVClientMessage.StatusType.UNSUBSCRIBE_ERROR);
        }
    }
}
