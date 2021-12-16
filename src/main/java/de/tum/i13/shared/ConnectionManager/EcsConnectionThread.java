package de.tum.i13.shared.ConnectionManager;

import de.tum.i13.shared.ConnectionManager.ConnectionManager;
import de.tum.i13.shared.ConnectionManager.ConnectionManagerInterface;
import de.tum.i13.server.kv.*;

import java.io.IOException;
import java.net.Socket;
import java.util.logging.Logger;

public class EcsConnectionThread extends Thread {
    private static final Logger LOGGER = Logger.getLogger(EcsConnectionThread.class.getName());

    private CommandProcessor cp;
    private Socket ecsSocket;
    private ConnectionManagerInterface ecsConnManager;

    public EcsConnectionThread(CommandProcessor commandProcessor, Socket ecsSocket){
        this.cp = commandProcessor;
        this.ecsSocket = ecsSocket;
    }

    @Override
    public void run(){
        cp.setServerState(ServerState.SERVER_STOPPED);
        cp.setEcsConnectionState(EcsConnectionState.READY_FOR_CONNECTION);
        try {
            ecsConnManager = new ConnectionManager(ecsSocket);
            System.out.println("Connected to ECS");
        } catch (IOException e) {
            LOGGER.warning(e.getMessage());
        }
        try {
            String initConnection = "add_server " + ecsSocket.getLocalAddress().toString().substring(1) + " " + ecsSocket.getLocalPort();
            ecsConnManager.send(initConnection);
            cp.setEcsConnectionState(EcsConnectionState.WAITING_FOR_INITIALIZATION);
            LOGGER.info("add_server request sent to ECS.");
            String resp;
            while ( (resp = ecsConnManager.receive()) != null) {
                String[] respParts = resp.split(" ");
                String command = respParts[0];
                ServerMessage serverMessage = null;
                LOGGER.info("Received command from ECS: " + command);
                if (command.equals("first_key_range") && cp.getEcsConnectionState() == EcsConnectionState.WAITING_FOR_INITIALIZATION){
                    cp.setDataRangeStart("00000000000000000000000000000000");
                    cp.setDataRangeEnd("ffffffffffffffffffffffffffffffff");
                    cp.setNextKVServer(cp);
                    cp.setPrevKVServer(cp);
                    cp.setEcsConnectionState(EcsConnectionState.WAITING_FOR_METADATA);
                }
                else if (command.equals("init_key_range")){
                    // Receive metadata and set (from Bilge)
                    //cp.setDataRangeStart();
                    //cp.setDataRangeEnd();
                    //cp.setNextKVServer();
                    //cp.setPrevKVServer();
                    cp.setEcsConnectionState(EcsConnectionState.READY_FOR_SERVER_DATA_RETRIEVAL);
                    ConnectionManager kvServerToRetrieveConn = createConnectionManager(respParts[3]);
                    kvServerToRetrieveConn.send("request_data " + respParts[1] + " " + respParts[2]);
                }
                else if (command.equals("update_metadata") && cp.getEcsConnectionState() == EcsConnectionState.WAITING_FOR_METADATA){
                    // Set inner metadata
                    cp.setServerState(ServerState.RUNNING);
                }
                else if (command.equals("request_data")){

                }
                else if (command.equals("send_data")){

                }
                else if (command.equals("health_check")){
                    serverMessage = new ServerMessageImpl(ServerMessage.StatusType.HEALTHY);
                    ecsConnManager.send(serverMessage.toString());
                }
                else {
                    serverMessage = new ServerMessageImpl(ServerMessage.StatusType.COMMAND_NOT_FOUND);
                    LOGGER.info("command not found");

                }
//                String res = cp.processEcsCommand(resp) + "\r\n";
//                this.connectionManager.send(res);
            }
//            String res = cp.processEcsCommand(resp) + "\r\n";
//            this.connectionManager.send(res);
        } catch (Exception ex) {
            LOGGER.warning(ex.getMessage());
            ex.printStackTrace();
        }

    }

    private ConnectionManager createConnectionManager(String addr) throws IOException {
        String[] parts = addr.split(":");
        String ip = parts[0];
        Integer port = Integer.parseInt(parts[1]);
        Socket socket = new Socket(ip, port);
        return new ConnectionManager(socket);
    }
}
