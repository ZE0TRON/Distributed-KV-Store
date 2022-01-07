package de.tum.i13.shared.ConnectionManager;

import de.tum.i13.server.kv.ServerMessage;
import de.tum.i13.server.kv.ServerMessageImpl;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

public class HeartbeatThread extends Thread {
    private static final Logger LOGGER = Logger.getLogger(HeartbeatThread.class.getName());
    private final ServerSocket ECS_HEARTBEAT_SOCKET;
    private ConnectionManagerInterface connectionManager;
    private Socket ecsSocket;
    private volatile boolean exit;


    public HeartbeatThread(ServerSocket ecsHeartbeatSocket){
        this.ECS_HEARTBEAT_SOCKET = ecsHeartbeatSocket;
        this.exit = false;
    }

    @Override
    public void run(){
        LOGGER.info("HeartbeatThread has been started. Waiting for ECS to connect.");
        try {
            ecsSocket = ECS_HEARTBEAT_SOCKET.accept();
            LOGGER.info("ECS has connected to heartbeat thread.");
        } catch (IOException e) {
            LOGGER.warning(e.getMessage());
        }
        try {
            this.connectionManager = new ConnectionManager(ecsSocket);
        } catch (IOException e){
            LOGGER.warning(e.getMessage());
        }
        try {
            String recv;
            while ( (recv = connectionManager.receive()) != null) {
                long start = System.currentTimeMillis();
                if (recv.equals("health_check")){
                    connectionManager.send(new ServerMessageImpl(ServerMessage.StatusType.HEALTHY).toString());
                    long timeItTakes = System.currentTimeMillis() - start;
                    if(timeItTakes >= 700) {
                        LOGGER.info("Sent heartbeat to ECS  less than " + timeItTakes + " ms.");
                    }
                }
                else {
                    connectionManager.send(new ServerMessageImpl(ServerMessage.StatusType.COMMAND_NOT_FOUND).toString());
                    LOGGER.info("Unknown command received from ECS in heartbeat thread.");
                }
            }
        } catch (Exception e) {
            LOGGER.warning(e.getMessage());
        }
    }
}
