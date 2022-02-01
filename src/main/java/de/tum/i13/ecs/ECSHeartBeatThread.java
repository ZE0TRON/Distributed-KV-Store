package de.tum.i13.ecs;

import de.tum.i13.ecs.cs.ECS;
import de.tum.i13.server.exception.CommunicationTerminatedException;
import de.tum.i13.shared.ConnectionManager.ConnectionManager;
import de.tum.i13.shared.ConnectionManager.ConnectionManagerInterface;
import de.tum.i13.shared.ConnectionManager.ServerConnectionThread;
import de.tum.i13.shared.Constants;
import de.tum.i13.shared.Server;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.logging.Logger;

public class ECSHeartBeatThread extends Thread {
    private static final Logger LOGGER = Logger.getLogger(ServerConnectionThread.class.getName());
    //public static HashMap<String, ConnectionManagerInterface> connections;

    private Socket kvServerSocket;
    private Server kvServer;
    private ConnectionManagerInterface connectionManager;

    public ECSHeartBeatThread(Server kvServer) throws IOException {
        this.kvServer = kvServer;
        this.kvServerSocket = new Socket(kvServer.getAddress(), Constants.HEARTBEAT_PORT);
    }

    @Override
    public void run() {
        try {
            this.connectionManager = new ConnectionManager(kvServerSocket);
            String recv, res;
            // 700 ms timeout
            kvServerSocket.setSoTimeout(700);
            connectionManager.send("health_check");
            while ((recv = connectionManager.receive()) != null) {
                if (!recv.equals("healthy")) {
                  throw new Exception("KVServer : " + kvServer.toHashableString() + " crashed" );
                }
                connectionManager.send("health_check");
            }
        }
        catch(Exception ex) {
            LOGGER.warning(ex.getMessage());
            ex.printStackTrace();
        }
        finally {
            ECS.getInstance().serverCrashed(kvServer);
        }
    }
}
