package de.tum.i13.shared.ConnectionManager;

import de.tum.i13.client.KeyRange;
import de.tum.i13.server.Main;
import de.tum.i13.server.kv.*;
import de.tum.i13.shared.Util;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

public class EcsConnectionThread extends Thread {
    private static final Logger LOGGER = Logger.getLogger(EcsConnectionThread.class.getName());

    private final CommandProcessor cp;
    private final KVCommandProcessor kvcp;
    private final Socket ecsSocket;
    private ConnectionManagerInterface ecsConnManager;
    public static ConnectionManagerInterface ECSConnection;
    private boolean threadAlive;
    public static volatile int handoverOperationCount;

    public EcsConnectionThread(CommandProcessor commandProcessor, KVCommandProcessor kvcp, Socket ecsSocket){
        this.cp = commandProcessor;
        this.ecsSocket = ecsSocket;
        this.kvcp = kvcp;
        this.threadAlive = true;

        try {
            ecsConnManager = new ConnectionManager(ecsSocket);
            ECSConnection = ecsConnManager;
            System.out.println("Connected to ECS");
        } catch (IOException e) {
            LOGGER.warning(e.getMessage());
        }
    }

    @Override
    public void run(){
        LOGGER.info("ECSThread has been started.");
        try {
            String myIP = ecsSocket.getLocalAddress().toString().substring(1);
            Main.serverIp = myIP;
            String initConnection = "add_server " + myIP + " " + Main.port + " " + ecsSocket.getLocalPort();
            ecsConnManager.send(initConnection);
            LOGGER.info("add_server request sent to ECS.");
            String resp;
            while (threadAlive  && (resp = ecsConnManager.receive()) != null) {
                String[] respParts = resp.split(" ");
                String command = respParts[0];
                ServerMessage serverMessage;
                LOGGER.info("Received command from ECS: " + resp);
                switch (command) {
                    case "first_key_range":
                        break;
                    case "init_key_range": {
                        // init_key_range <key_range_from_third_precedent_to_itself> <first_successor_kv_server_addr>
                        // example: init_key_range <from> <to> <addr>:<port>
                        String replicaRangeFrom = respParts[1];
                        String replicaRangeTo = respParts[2];
                        Socket kvServerToCommunicateSocket = createSocket(respParts[3]);
                        String payload = "request_data " + replicaRangeFrom + " " + replicaRangeTo;
                        LOGGER.info("Sending " + payload + " to " + respParts[3]);
                        ConnectionThread connectionThread = new ConnectionThread(cp, kvcp, kvServerToCommunicateSocket,  payload);
                        connectionThread.start();
                        break;
                    }
                    case "handover_start": { // handover_start from to ip:port from to ip:port from to ip:port
                        CommandProcessor.serverState = ServerState.SERVER_WRITE_LOCK;
                        if (respParts.length == 10){
                            LOGGER.info("ECSThread received three handover_start requests.");
                            handoverOperationCount = 3;
                        }
                        else if (respParts.length == 4){
                            LOGGER.info("ECSThread received a single handover_start request.");
                            handoverOperationCount = 1;
                        }
                        else {
                            LOGGER.warning("ECSThread received a handover_start request with an incorrect format!");
                            break;
                        }
                        for (int i=0, request_count=handoverOperationCount; i<request_count; i++){
                            String payload = "handover_data " + respParts[3*i+1] + " " + respParts[3*i+2];
                            Socket kvServerToCommunicateSocket = createSocket(respParts[3*i+3]);
                            ConnectionThread connectionThread = new ConnectionThread(cp, kvcp, kvServerToCommunicateSocket,  payload);
                            connectionThread.start();
                        }
                        break;
                    }
                    case "update_metadata":
                        if(respParts.length == 1) {
                            return;
                        }
                        String replicaMetadataStr;
                        if(respParts.length > 3){
                            KVStoreImpl.setFirstSuccessor(respParts[3]);
                            KVStoreImpl.setSecondSuccessor(respParts[4]);
                            KVStoreImpl.setReplicaConnections();
                            replicaMetadataStr = respParts[2];
                        }
                        else {
                            replicaMetadataStr = respParts[1];
                        }

                        String coordinatorMetadataStr = respParts[1];

                        String[] coordinatorKeyRanges = coordinatorMetadataStr.split(";");
                        String[] replicaKeyRanges = replicaMetadataStr.split(";");

                        ArrayList<KeyRange> coordinatorMetadata = new ArrayList<>();
                        ArrayList<KeyRange> replicaMetadata = new ArrayList<>();

                        Util.parseKeyrange(coordinatorKeyRanges,coordinatorMetadata);
                        Util.parseKeyrange(replicaKeyRanges,replicaMetadata);

                        KVStoreImpl kvStoreInstance = new KVStoreImpl();
                        kvStoreInstance.updateKeyRange(coordinatorMetadata, coordinatorMetadataStr, "coordinator");
                        kvStoreInstance.updateKeyRange(replicaMetadata, replicaMetadataStr, "replica");

                        kvStoreInstance.dropKeys();

                        LOGGER.info("Entering RUNNING state.");
                        CommandProcessor.serverState = ServerState.RUNNING;
                        break;
                    default:
                        serverMessage = new ServerMessageImpl(ServerMessage.StatusType.COMMAND_NOT_FOUND);
                        LOGGER.info("command not found");
                        ecsConnManager.send(serverMessage.toString());
                        break;
                }
            }
        } catch (Exception ex) {
            LOGGER.warning(ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void kill(){
        this.threadAlive = false;
        ecsConnManager.disconnect();
    }

    private Socket createSocket(String addr) throws IOException {
        String[] parts = addr.split(":");
        String ip = parts[0];
        int port = Integer.parseInt(parts[1]);
        Socket socket = new Socket(ip, port);
        LOGGER.info("ConnectionManager has been created with addr: " + addr);
        return socket;
    }
}
