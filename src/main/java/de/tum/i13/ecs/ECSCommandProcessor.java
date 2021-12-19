package de.tum.i13.ecs;

import de.tum.i13.server.exception.CommunicationTerminatedException;
import de.tum.i13.server.kv.CommandProcessorInterface;
import de.tum.i13.shared.Pair;
import de.tum.i13.shared.Server;
import de.tum.i13.ecs.cs.ConfigurationService;


import java.util.logging.Logger;

public class ECSCommandProcessor implements CommandProcessorInterface {
    private static final Logger LOGGER = Logger.getLogger(ECSCommandProcessor.class.getName());
    private final ConfigurationService cs;
    public ECSCommandProcessor(ConfigurationService cs) {
        this.cs = cs;
    }

    @Override
    public String process(String command) throws CommunicationTerminatedException {
        ECSMessage ecsMessage = null;
        String[] parts = command.split(" ");
        if (parts.length == 0) {
           parts = new String[]{"help"};
        }
        Server server;
        LOGGER.info("received command " + command);
            switch (parts[0]) {
                case "add_server":
                    server = new Server(parts[1], parts[2], parts[3]);
                    cs.addServer(server);
                    break;
                case "handover_complete":
                    cs.handoverFinished(new Pair<>(parts[1], parts[2]));
                    throw new CommunicationTerminatedException();
                case "shutdown":
                    server = new Server(parts[1], parts[2]);
                    cs.deleteServer(server);
                    break;
                default:
                    LOGGER.info("command not found");
            }
            return ecsMessage != null ?  ecsMessage.toString() : null;
    }
}
