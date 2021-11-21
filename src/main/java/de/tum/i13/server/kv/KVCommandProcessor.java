package de.tum.i13.server.kv;

import de.tum.i13.server.storageManagment.CacheManagerLFU;
import de.tum.i13.shared.CommandProcessor;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.logging.Logger;

public class KVCommandProcessor implements CommandProcessor {
    private KVStore kvStore;
    private static final Logger LOGGER = Logger.getLogger(KVCommandProcessor.class.getName());
    public KVCommandProcessor(KVStore kvStore) {
        this.kvStore = kvStore;
    }

    @Override
    public String process(String command) {
        KVMessage kvMessage;
        String[] parts = command.split(" ");
        if (parts.length == 0) {
           parts = new String[]{"help"};
        }
        LOGGER.info("received command " + command);
        try {
            switch (parts[0]) {
                case "put":
                    if (parts.length < 2) {
                        kvMessage = kvStore.commandNotFound(command);
                        break;
                    }
                    else if (parts.length == 2) {
                        kvMessage = kvStore.put(parts[1], null);
                    }
                    else {
                        String args = command.substring(command.indexOf(' ') + 1);
                        String value = args.substring(args.indexOf(' ') + 1);
                        kvMessage = kvStore.put(parts[1], value);
                    }
                    break;
                case "get":
                    if (parts.length < 2) {
                        kvMessage = kvStore.commandNotFound(command);
                        break;
                    }
                    kvMessage = kvStore.get(parts[1]);
                    break;
                case "delete":
                    if (parts.length < 2) {
                        kvMessage = kvStore.commandNotFound(command);
                        break;
                    }
                    kvMessage = kvStore.put(parts[1], null);
                    break;
                default:
                    LOGGER.info("command not found");
                    kvMessage = kvStore.commandNotFound(command);
            }
            return kvMessage.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String connectionAccepted(InetSocketAddress address, InetSocketAddress remoteAddress) {
        //TODO
        LOGGER.fine("Connection accepted address: " + address + " remote address: " + remoteAddress);
        return new KVMessageImpl(null, null, KVMessage.StatusType.CONNECTION_ESTABLISHED).toString();
    }

    @Override
    public void connectionClosed(InetAddress address) {
        LOGGER.fine("Connection closed address: " + address );
        //TODO
    }
}
