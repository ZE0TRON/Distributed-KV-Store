package de.tum.i13.shared.ConnectionManager;

import de.tum.i13.shared.CommandProcessor;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.logging.Logger;

public class ClientConnectionThread extends Thread {
    private static final Logger LOGGER = Logger.getLogger(ClientConnectionThread.class.getName());

    private CommandProcessor cp;
    private KVCommandProcessor kvScp;
    private Socket clientSocket;
    private ConnectionManagerInterface connectionManager;

    public ClientConnectionThread(CommandProcessor commandProcessor, KVCommandProcessor kvCommandProcessor, Socket clientSocket) {
        this.cp = commandProcessor;
        this.clientSocket = clientSocket;
        this.kvScp = kvCommandProcessor;
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
            String res = "Connection established";
            this.connectionManager.send(res);
            LOGGER.info("Response sent");
            String recv;
            while ( (recv = connectionManager.receive()) != null) {
                res = cp.process(recv) + "\r\n";
                this.connectionManager.send(res);
            }
        } catch(Exception ex) {
            LOGGER.warning(ex.getMessage());
            ex.printStackTrace();
        }
    }
}
