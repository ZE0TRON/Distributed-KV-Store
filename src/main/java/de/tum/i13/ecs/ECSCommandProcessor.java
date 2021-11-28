package de.tum.i13.ecs;

import de.tum.i13.ecs.cs.ConfigurationService;
import de.tum.i13.server.kv.KVMessage;
import de.tum.i13.server.kv.KVMessageImpl;
import de.tum.i13.server.kv.KVStore;
import de.tum.i13.shared.CommandProcessor;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.logging.Logger;

public class ECSCommandProcessor implements CommandProcessor {
    private static final Logger LOGGER = Logger.getLogger(ECSCommandProcessor.class.getName());
    private final ConfigurationService cs;
    public ECSCommandProcessor(ConfigurationService cs) {
        this.cs = cs;
    }

    @Override
    public String process(String command) {
        ECSMessage ecsMessage = null;
        String[] parts = command.split(" ");
        if (parts.length == 0) {
           parts = new String[]{"help"};
        }
        LOGGER.info("received command " + command);
        try {
            switch (parts[0]) {
                case "a":
                    break;
                default:
                    LOGGER.info("command not found");
            }
            return ecsMessage.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String connectionAccepted(InetSocketAddress address, InetSocketAddress remoteAddress) {
        //TODO
        LOGGER.fine("Connection accepted address: " + address + " remote address: " + remoteAddress);
        return new ECSMessageImpl(null, null, ECSMessage.StatusType.CONNECTION_ESTABLISHED).toString();
    }

    @Override
    public void connectionClosed(InetAddress address) {
        LOGGER.fine("Connection closed address: " + address );
        //TODO
    }
}
