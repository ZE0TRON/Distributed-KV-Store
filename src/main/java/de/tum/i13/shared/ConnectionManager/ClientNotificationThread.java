package de.tum.i13.shared.ConnectionManager;

import de.tum.i13.server.kv.KVClientMessage;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Locale;
import java.util.logging.Logger;

public class ClientNotificationThread extends Thread {
    private static final Logger LOGGER = Logger.getLogger(ClientNotificationThread.class.getName());
    private final ServerSocket CLIENT_NOTIFICATION_SOCKET;
    private ConnectionManagerInterface connectionManager;

    public ClientNotificationThread(ServerSocket client_notification_socket) {
        CLIENT_NOTIFICATION_SOCKET = client_notification_socket;
    }

    @Override
    public void run(){
        LOGGER.info("ClientNotificationThread has been started. Waiting for ECS to connect.");
        try {
            Socket serverNotificationSocket = CLIENT_NOTIFICATION_SOCKET.accept();
            LOGGER.info("Server has connected to notification thread.");
            this.connectionManager = new ConnectionManager(serverNotificationSocket);
        } catch (IOException e){
            LOGGER.warning(e.getMessage());
        }

        try {
            String recv;
            while ( (recv = connectionManager.receive()) != null) {

                String[] parts = recv.split(" ");
                if (parts[0].equals(KVClientMessage.StatusType.NOTIFICATION_UPDATE.toString().toLowerCase(Locale.ROOT))) {
                    // notification_update key value
                    System.out.println("EchoClientNotification> UPDATE key: " + parts[1] + " new value: " + parts[2]);
                } else if (parts[0].equals(KVClientMessage.StatusType.NOTIFICATION_DELETE.toString().toLowerCase(Locale.ROOT))) {
                    // notification_delete key value
                    System.out.println("EchoClientNotification> DELETE key: " + parts[1] + " value: " + parts[2]);
                } else {
                    throw new RuntimeException("Unknown notification type");
                }
            }
        } catch (Exception e) {
            LOGGER.warning(e.getMessage());
        }
    }
}
