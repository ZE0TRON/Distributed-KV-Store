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
           parts = new String[]{"help"};
        }
        LOGGER.info("Command Processor: Received command " + command);
        LOGGER.info("KVServer state: " + serverState);
        String commandType = parts[0];
        try {
            if (serverState.equals(ServerState.SERVER_STOPPED)){
                kvClientMessage = new KVClientMessageImpl(null,null, KVClientMessage.StatusType.SERVER_STOPPED);
            }
            else if (serverState.equals(ServerState.SERVER_WRITE_LOCK) && (parts[0].equals("put") || parts[0].equals("delete"))){
                kvClientMessage = new KVClientMessageImpl(null,null, KVClientMessage.StatusType.SERVER_WRITE_LOCK);
            }
            else {  // (ServerState.RUNNING) or (ServerState.SERVER_WRITE_LOCK and command is one of GET, KEYRANGE, KEYRANGE_READ)
                KeyRange coordinatorKeyRange = KVStoreImpl.getCoordinatorKeyRange();
                KeyRange wholeKeyRange = KVStoreImpl.getWholeKeyRange();
                switch (parts[0]) {
                        case "put":
                            if (parts.length < 2) {
                                kvClientMessage = kvStore.commandNotFound(command);
                                break;
                            }
                            else if (!Util.isKeyInRange(coordinatorKeyRange.from, coordinatorKeyRange.to, ConsistentHashingService.findHash(parts[1]))) {
                                kvClientMessage = new KVClientMessageImpl( null/*KVStoreImpl.getMetaDataString()*/, null, KVClientMessage.StatusType.SERVER_NOT_RESPONSIBLE);
                                LOGGER.info("KVServer: SERVER_NOT_RESPONSIBLE occurred.");
                                break;
                            }
                            if (parts.length == 2) {
                                kvClientMessage = kvStore.put(parts[1], null, "kvClient");
                            }
                            else {
                                String args = command.substring(command.indexOf(' ') + 1);
                                String value = args.substring(args.indexOf(' ') + 1);
                                kvClientMessage = kvStore.put(parts[1], value, "kvClient");
                            }
                            break;
                        case "get":
                            if (parts.length < 2) {
                                kvClientMessage = kvStore.commandNotFound(command);
                                break;
                            }
                            else if (!Util.isKeyInRange(wholeKeyRange.from, wholeKeyRange.to, ConsistentHashingService.findHash(parts[1]))) {
                                kvClientMessage = new KVClientMessageImpl( null/*KVStoreImpl.getMetaDataString()*/, null, KVClientMessage.StatusType.SERVER_NOT_RESPONSIBLE);
                                LOGGER.info("KVServer: SERVER_NOT_RESPONSIBLE occurred.");
                                break;
                            }
                            kvClientMessage = kvStore.get(parts[1]);
                            break;
                        case "delete":
                            if (parts.length < 2) {
                                kvClientMessage = kvStore.commandNotFound(command);
                                break;
                            }
                            else if (!Util.isKeyInRange(coordinatorKeyRange.from, coordinatorKeyRange.to, ConsistentHashingService.findHash(parts[1]))) {
                                kvClientMessage = new KVClientMessageImpl( null/*KVStoreImpl.getMetaDataString()*/, null, KVClientMessage.StatusType.SERVER_NOT_RESPONSIBLE);
                                LOGGER.info("KVServer: SERVER_NOT_RESPONSIBLE occurred.");
                                break;
                            }
                            kvClientMessage = kvStore.put(parts[1], null, "kvClient");
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
                }
            return kvClientMessage.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
