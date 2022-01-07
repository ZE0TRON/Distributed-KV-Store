package de.tum.i13.ecs;

import de.tum.i13.server.exception.CommunicationTerminatedException;
import de.tum.i13.server.kv.CommandProcessorInterface;
import de.tum.i13.shared.ConnectionManager.ConnectionManager;
import de.tum.i13.shared.ConnectionManager.ConnectionManagerInterface;
import de.tum.i13.shared.ConnectionManager.ServerConnectionThread;
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
                    ConnectionManagerInterface cm = ServerConnectionThread.connections.get(server.toEcsConnectionString());
                    ServerConnectionThread.connections.remove(server.toEcsConnectionString());
                    ServerConnectionThread.connections.put(server.toHashableString(), cm);
                    cs.addServer(server);
                    break;
                case "handover_complete":
                    boolean endConnection = cs.handoverFinished(new Pair<>(parts[1], parts[2]));
                    if (endConnection) {
                        throw new CommunicationTerminatedException();
                    }
                    break;
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
