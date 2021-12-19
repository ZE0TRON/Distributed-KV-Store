package de.tum.i13.ecs;

import de.tum.i13.shared.Pair;
import de.tum.i13.shared.Server;
import de.tum.i13.ecs.cs.ConfigurationService;
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
        Server server;
        LOGGER.info("received command " + command);
        try {
            switch (parts[0]) {
                case "add_server":
                    server = new Server(parts[1], parts[2]);
                    cs.addServer(server);
                    break;
                case "handover_complete":
                    cs.handoverFinished(new Pair<>(parts[1], parts[2]));
                    break;
                case "shutdown":
                    server = new Server(parts[1], parts[2]);
                    cs.deleteServer(server);
                    break;
                default:
                    LOGGER.info("command not found");
            }
            return ecsMessage != null ?  ecsMessage.toString() : null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
