package de.tum.i13.shared.ConnectionManager;

import de.tum.i13.client.KeyRange;
import de.tum.i13.ecs.cs.ECS;
import de.tum.i13.shared.ConnectionManager.ConnectionManager;
import de.tum.i13.shared.ConnectionManager.ConnectionManagerInterface;
import de.tum.i13.server.kv.*;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Logger;

public class EcsConnectionThread extends Thread {
    private static final Logger LOGGER = Logger.getLogger(EcsConnectionThread.class.getName());

    private CommandProcessor cp;
    private KVCommandProcessor kvcp;
    private Socket ecsSocket;
    private ConnectionManagerInterface ecsConnManager;
    public static ConnectionManagerInterface ECSConnection;

    public EcsConnectionThread(CommandProcessor commandProcessor, KVCommandProcessor kvcp, Socket ecsSocket){
        this.cp = commandProcessor;
        this.ecsSocket = ecsSocket;
        this.kvcp = kvcp;
    }

    @Override
    public void run(){
        cp.setServerState(ServerState.SERVER_STOPPED);
        cp.setEcsConnectionState(EcsConnectionState.READY_FOR_CONNECTION);
        try {
            ecsConnManager = new ConnectionManager(ecsSocket);
            ECSConnection = ecsConnManager;
            System.out.println("Connected to ECS");
        } catch (IOException e) {
            LOGGER.warning(e.getMessage());
        }
        try {
            String initConnection = "add_server " + ecsSocket.getLocalAddress().toString().substring(1) + " " + ecsSocket.getLocalPort();
            ecsConnManager.send(initConnection);
            //cp.setEcsConnectionState(EcsConnectionState.WAITING_FOR_INITIALIZATION);
            LOGGER.info("add_server request sent to ECS.");
            String resp;
            while ( (resp = ecsConnManager.receive()) != null) {
                String[] respParts = resp.split(" ");
                String command = respParts[0];
                ServerMessage serverMessage;
                LOGGER.info("Received command from ECS: " + command);
                switch (command) {
                    case "first_key_range":
                        //cp.setEcsConnectionState(EcsConnectionState.WAITING_FOR_METADATA);
                        break;
                    case "init_key_range": {
                        ConnectionManager kvServerToRetrieveConn = createConnectionManager(respParts[3]);
                        kvServerToRetrieveConn.send("request_data " + respParts[1] + " " + respParts[2]);
                        ConnectionThread connectionThread = new ConnectionThread(cp, kvcp, kvServerToRetrieveConn.getSocket(), false);
                        connectionThread.start();
                        break;
                    }
                    case "handover_start": {
                        //cp.setEcsConnectionState(EcsConnectionState.INITIALIZED_KVSERVER_WAITING_FOR_DATA);
                        ConnectionManager kvServerToRetrieveConn = createConnectionManager(respParts[3]);
                        kvServerToRetrieveConn.send("handover_data " + respParts[1] + " " + respParts[2]);
                        ConnectionThread connectionThread = new ConnectionThread(cp, kvcp, kvServerToRetrieveConn.getSocket(), false);
                        connectionThread.start();
                        break;
                    }
                    case "update_metadata":
                        int index = resp.indexOf("update_metadata");
                        String metadataString = resp.split(" ")[1];
                        String[] keyRanges = resp.substring(index + "update_metadata".length() + 1).split(";");
                        ArrayList<KeyRange> metaData = new ArrayList<>();
                        for (String keyRange : keyRanges) {
                            String[] parts = keyRange.split(",");
                            if (parts.length != 3 || !parts[2].contains(":")) {
                                throw new RuntimeException("Invalid key range: " + keyRange);
                            }
                            String from = parts[0];
                            String to = parts[1];
                            index = parts[2].indexOf(":");
                            String serverIP = parts[2].substring(0, index);
                            int serverPort = Integer.parseInt(parts[2].substring(index + 1));
                            metaData.add(new KeyRange(from, to, serverIP, serverPort));
                        }
                        KVStoreImpl.metaDataString = metadataString;
                        KVStoreImpl.metaData = metaData;
                        break;
                    case "health_check":
                        serverMessage = new ServerMessageImpl(ServerMessage.StatusType.HEALTHY);
                        ecsConnManager.send(serverMessage.toString());
                        break;
                    default:
                        serverMessage = new ServerMessageImpl(ServerMessage.StatusType.COMMAND_NOT_FOUND);
                        LOGGER.info("command not found");
                        break;
                }
                // TODO: return result
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
        int port = Integer.parseInt(parts[1]);
        Socket socket = new Socket(ip, port);
        return new ConnectionManager(socket);
    }
}
