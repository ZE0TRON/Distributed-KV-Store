package de.tum.i13.shared.ConnectionManager;

import de.tum.i13.server.kv.KVCommandProcessor;
import de.tum.i13.shared.CommandProcessor;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

public class ConnectionThread extends Thread {
    private static final Logger LOGGER = Logger.getLogger(ConnectionThread.class.getName());

    private CommandProcessor cp;
    private KVCommandProcessor kvScp;
    private Socket clientSocket;
    private ConnectionManagerInterface connectionManager;
    private final ArrayList<String> KVServerCommands = new ArrayList<>(Arrays.asList("", "", "", ""));
    private final boolean receivedConnection;

    public ConnectionThread(CommandProcessor commandProcessor, KVCommandProcessor kvCommandProcessor, Socket clientSocket, boolean receviedConnection) {
        this.cp = commandProcessor;
        this.clientSocket = clientSocket;
        this.kvScp = kvCommandProcessor;
        this.receivedConnection = receviedConnection;
    }

    // TODO put KV sync function
    // TODO put Persistent sync function
    // TODO delete same
    @Override
    public void run() {
        try {
            this.connectionManager = new ConnectionManager(clientSocket);
        } catch (IOException e) {
            LOGGER.warning(e.getMessage());
        }
        try {
            String res;
            if(receivedConnection) {
                res = "Connection established";
                this.connectionManager.send(res);
                LOGGER.info("Response sent");
            }
            String recv;

            while ( (recv = connectionManager.receive()) != null) {
                String[] parts = recv.split(" ");

                if (KVServerCommands.contains(parts[0])) {
                    res = kvScp.process(recv) + "\r\n";
                }
                else {
                    res = cp.process(recv) + "\r\n";
                }
                if(res != null) {
                    this.connectionManager.send(res);
                }
            }
        } catch(Exception ex) {
            LOGGER.warning(ex.getMessage());
            ex.printStackTrace();
        }
    }
}
