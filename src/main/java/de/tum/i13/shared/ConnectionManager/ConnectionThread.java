package de.tum.i13.shared.ConnectionManager;

import de.tum.i13.server.exception.CommunicationTerminatedException;
import de.tum.i13.server.kv.CommandProcessor;
import de.tum.i13.server.kv.KVCommandProcessor;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

public class ConnectionThread extends Thread {
    private static final Logger LOGGER = Logger.getLogger(ConnectionThread.class.getName());

    private final CommandProcessor cp;
    private final KVCommandProcessor kvScp;
    private final Socket clientSocket;
    private ConnectionManagerInterface connectionManager;
    private final ArrayList<String> KVServerCommands = new ArrayList<>(Arrays.asList("request_data", "send_data", "ack_data", "handover_data", "handover_ack", "Connection", "replica_active", "replica_deactive"));
    public static boolean CanShutdown;
    private String initialPayload;

    public ConnectionThread(CommandProcessor commandProcessor, KVCommandProcessor kvCommandProcessor, Socket clientSocket, String initialPayload) {
        this.cp = commandProcessor;
        this.clientSocket = clientSocket;
        this.kvScp = kvCommandProcessor;
        ConnectionThread.CanShutdown = false;
        this.initialPayload = initialPayload;
    }

    // TODO put KV sync function
    // TODO put Persistent sync function
    // TODO delete same
    @Override
    public void run() {
        LOGGER.info("ConnectionThread has been started.");
        try {
            this.connectionManager = new ConnectionManager(clientSocket);
        } catch (IOException e) {
            LOGGER.warning(e.getMessage());
        }
        try {
            String res;
            this.connectionManager.send(initialPayload);
            LOGGER.info("Response sent.");
            String recv;

            while ( (recv = connectionManager.receive()) != null) {
                String command = recv.split(" ")[0];
                if(recv.equals("Connection established.")) {
                    continue;
                }
                LOGGER.info("Received command in ConnectionThread: " + recv);
                try {
                    if (KVServerCommands.contains(command)) {
                        LOGGER.info("KVServerCommand has been received. Now being processed.");
                        res = kvScp.process(recv) + "\r\n";
                    }
                    else {
                        LOGGER.info("ClientCommand has been received. Now being processed.");
                        res = cp.process(recv) + "\r\n";
                    }
                    if(res != null) {
                        LOGGER.info("ConnectionThread response being sent.");
                        this.connectionManager.send(res);
                    }
                    res = null;
                } catch (CommunicationTerminatedException ex) {
                    ConnectionThread.CanShutdown = true;
                    break;
                } catch (Exception ex) {
                    LOGGER.warning(ex.getMessage());
                    ex.printStackTrace();
                }

            }
        } catch(Exception ex) {
            LOGGER.warning(ex.getMessage());
            ex.printStackTrace();
        }
    }
}
