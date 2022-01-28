package de.tum.i13.shared.ConnectionManager;

import de.tum.i13.shared.ClientConfig;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

public class ClientNotificationHandlerThread extends Thread{
    private static final Logger LOGGER = Logger.getLogger(ClientNotificationHandlerThread.class.getName());
    private final ClientConfig cfg;
    ServerSocket notificationThreadServerSocket = null;
    private static boolean exit;

    public ClientNotificationHandlerThread(ClientConfig cfg) {
        this.cfg = cfg;
        exit = false;
    }

    @Override
    public void run(){
        LOGGER.info("ClientNotificationThread has been started.");

        try {
            notificationThreadServerSocket = new ServerSocket();
            notificationThreadServerSocket.bind(new InetSocketAddress(cfg.listenaddr, cfg.port));
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (!exit){
            try {
                Socket notificationThreadSocket = notificationThreadServerSocket.accept();
                LOGGER.finest("Server connected for notification");
                ClientNotificationThread clientNotificationThread = new ClientNotificationThread(notificationThreadSocket);
                clientNotificationThread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void kill(){
        exit = true;
    }
}