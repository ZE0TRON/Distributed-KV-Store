package de.tum.i13.shared.ConnectionManager;

import de.tum.i13.ecs.ECSCommandProcessor;
import de.tum.i13.server.exception.CommunicationTerminatedException;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.logging.Logger;

public class ServerConnectionThread extends Thread {
    private static final Logger LOGGER = Logger.getLogger(ServerConnectionThread.class.getName());
    public static HashMap<String, ConnectionManagerInterface> connections;

    private final ECSCommandProcessor cp;
    private Socket clientSocket;
    private ConnectionManagerInterface connectionManager;

    public ServerConnectionThread(ECSCommandProcessor commandProcessor, Socket clientSocket) {
        this.cp = commandProcessor;
        this.clientSocket = clientSocket;
        if(connections == null) {
            connections = new HashMap<>();
        }
    }

    // TODO put KV sync function
    // TODO put Persistent sync function
    // TODO delete same
    @Override
    public void run() {
        try {
            this.connectionManager = new ConnectionManager(clientSocket);
            String serverString = clientSocket.getInetAddress().toString().substring(1) + ":" + clientSocket.getPort();
            connections.put(serverString, this.connectionManager);
            LOGGER.info("Connection established with server: " + serverString );
        } catch (IOException e) {
            LOGGER.warning(e.getMessage());
        }
        try {
            String recv, res;
            while ( (recv = connectionManager.receive()) != null) {
                try {
                    res = cp.process(recv);
                    if (res != null) {
                        this.connectionManager.send(res + "\r\n");
                    }
                } catch (CommunicationTerminatedException ex) {
                    String serverString = clientSocket.getInetAddress().toString().substring(1) + ":" + clientSocket.getPort();
                    connections.remove(serverString);
                    clientSocket.close();
                    clientSocket = null;
                    break;
                } catch (Exception e) {
                    LOGGER.warning(e.getMessage());
                }
            }
        } catch(Exception ex) {
            LOGGER.warning(ex.getMessage());
            ex.printStackTrace();
        }
    }
}
