package de.tum.i13.server.thread;

import de.tum.i13.server.ConnectionManager.ConnectionManager;
import de.tum.i13.server.ConnectionManager.ConnectionManagerInterface;
import de.tum.i13.shared.CommandProcessor;

import java.io.*;
import java.net.Socket;

public class ConnectionHandleThread extends Thread {
    private CommandProcessor cp;
    private Socket clientSocket;
    private ConnectionManagerInterface connectionManager;

    public ConnectionHandleThread(CommandProcessor commandProcessor, Socket clientSocket) {
        this.cp = commandProcessor;
        this.clientSocket = clientSocket;
        System.out.println("Init Thread");

    }

    // TODO put KV sync function
    // TODO put Persistent sync function
    // TODO delete same
    @Override
    public void run() {
        try {
            this.connectionManager = new ConnectionManager(clientSocket);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        try {
            String res = "Connection established";
            this.connectionManager.send(res);
            System.out.println("Response sent");
            String recv;
            while ( (recv = connectionManager.receive()) != null) {
                res = cp.process(recv);
                this.connectionManager.send(res);
            }
        } catch(Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
    }
}
