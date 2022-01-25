package de.tum.i13.shared.ConnectionManager;

import de.tum.i13.server.kv.KVClientMessage;
import de.tum.i13.server.kv.KVClientMessageImpl;

import de.tum.i13.shared.Server;

import java.io.IOException;
import java.net.Socket;
import java.util.logging.Logger;

public class NotificationThread extends Thread {
    private static final Logger LOGGER = Logger.getLogger(NotificationThread.class.getName());
    private final Server client;
    private final String key;
    private final String newValue;
    private final KVClientMessage.StatusType notificationType;


    public NotificationThread(String key, String newValue, KVClientMessage.StatusType notificationType, Server client){
        this.client = client;
        this.key = key;
        this.newValue = newValue;
        this.notificationType = notificationType;
    }

    @Override
    public void run(){
        try {
            Socket clientSocket = new Socket(client.getAddress(), Integer.parseInt(client.getPort()));
            ConnectionManager connection = new ConnectionManager(clientSocket);
            KVClientMessage res = new KVClientMessageImpl(key, newValue, notificationType);
            connection.send(res + "\r\n");
            connection.disconnect();
        } catch (IOException e) {
            LOGGER.warning("Exception while sending notification to client " + e.getMessage());
        }
    }
}
