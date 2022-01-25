package de.tum.i13.shared.ConnectionManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

public class ClientNotificationThread extends Thread {
    private static final Logger LOGGER = Logger.getLogger(ClientNotificationThread.class.getName());
    private final ServerSocket CLIENT_NOTIFICATION_SOCKET;
    private Socket serverNotificationSocket;
    private ConnectionManagerInterface connectionManager;


    public ClientNotificationThread(ServerSocket client_notification_socket) {
        CLIENT_NOTIFICATION_SOCKET = client_notification_socket;
    }

    @Override
    public void run(){
        LOGGER.info("ClientNotificationThread has been started. Waiting for ECS to connect.");
        try {
            serverNotificationSocket = CLIENT_NOTIFICATION_SOCKET.accept();
            LOGGER.info("Server has connected to notification thread.");
        } catch (IOException e) {
            LOGGER.warning(e.getMessage());
        }
        try {
            this.connectionManager = new ConnectionManager(serverNotificationSocket);
        } catch (IOException e){
            LOGGER.warning(e.getMessage());
        }

        try {
            String recv;
            while ( (recv = connectionManager.receive()) != null) {
                System.out.println(recv);
            }
        } catch (Exception e) {
            LOGGER.warning(e.getMessage());
        }
    }
}
