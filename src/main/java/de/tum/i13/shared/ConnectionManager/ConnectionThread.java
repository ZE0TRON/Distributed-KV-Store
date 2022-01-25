package de.tum.i13.shared.ConnectionManager;

import de.tum.i13.server.exception.CommunicationTerminatedException;
import de.tum.i13.server.kv.*;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.logging.Logger;

public class ConnectionThread extends Thread {
    private static final Logger LOGGER = Logger.getLogger(ConnectionThread.class.getName());

    private final CommandProcessor cp;
    private final KVCommandProcessor kvScp;
    private final Socket clientSocket;
    private ConnectionManagerInterface connectionManager;
    private final ArrayList<String> KVServerCommands = new ArrayList<>(Arrays.asList("request_data", "send_data", "ack_data", "handover_data", "handover_ack", "Connection",
            "put_replica", "put_replica_ack", "put_replica_data", "put_replica_data_ack",
            "delete_replica", "delete_replica_ack", "delete_replica_data", "delete_replica_data_ack"));
    public static boolean CanShutdown;
    private String initialPayload;

    public ConnectionThread(CommandProcessor commandProcessor, KVCommandProcessor kvCommandProcessor, Socket clientSocket, String initialPayload) {
        this.cp = commandProcessor;
        this.clientSocket = clientSocket;
        this.kvScp = kvCommandProcessor;
        //TODO: Could not find any CanShutdown check?
        ConnectionThread.CanShutdown = false;
        this.initialPayload = initialPayload;
    }

    // TODO put KV sync function
    // TODO put Persistent sync function
    // TODO delete same
    @Override
    public void run() {
        LOGGER.info("ConnectionThread has been started.");
        try {
            this.connectionManager = new ConnectionManager(clientSocket);
        } catch (IOException e) {
            LOGGER.warning(e.getMessage());
        }
        try {
            String res;
            this.connectionManager.send(initialPayload);
            LOGGER.info("Response sent.");
            String recv;

            while ( (recv = connectionManager.receive()) != null) {
                String command = recv.split(" ")[0];
                if(recv.equals("Connection established.")) {
                    continue;
                }
                LOGGER.info("Received command in ConnectionThread: " + recv);
                try {
                    if (KVServerCommands.contains(command)) {
                        LOGGER.info("KVServerCommand has been received. Now being processed.");
                        res = kvScp.process(recv) + "\r\n";
                    }
                    else {
                        LOGGER.info("ClientCommand has been received. Now being processed.");
                        res = cp.process(recv) + "\r\n";

                        String replicaCommand = replicaCommand(res);
                        if (replicaCommand != null) {
                            CommandProcessor cp = new CommandProcessor(KVStoreImpl.getInstance());
                            KVCommandProcessor KVcp = new KVCommandProcessor(new KVTransferService(KVStoreImpl.getInstance()));

                            String[] parts = recv.split(" ");

                            Thread th = new ConnectionThread(cp, KVcp, KVStoreImpl.replica1Connection, replicaCommand + " " + parts[1]);
                            th.start();

                            Thread th2 = new ConnectionThread(cp, KVcp, KVStoreImpl.replica2Connection, replicaCommand + " " + parts[1]);
                            th2.start();
                        }
                    }
                    if(res != null) {
                        LOGGER.info("ConnectionThread response being sent.");
                        this.connectionManager.send(res);
                    }
                    res = null;
                } catch (CommunicationTerminatedException ex) {
                    ConnectionThread.CanShutdown = true;
                    break;
                } catch (Exception ex) {
                    LOGGER.warning(ex.getMessage());
                    ex.printStackTrace();
                }

            }
        } catch(Exception ex) {
            LOGGER.warning(ex.getMessage());
            ex.printStackTrace();
        }
    }

    private String replicaCommand(String res) {
        LOGGER.info("replica1Connection : " + KVStoreImpl.replica1Connection + "replica1Connection : " + KVStoreImpl.replica2Connection + " res is:" + res);
        if (KVStoreImpl.replica1Connection == null || KVStoreImpl.replica2Connection == null || res == null || res.length() == 0) {
            return null;
        }

        if (res.startsWith(KVClientMessage.StatusType.PUT_SUCCESS.toString().toLowerCase(Locale.ROOT))  ||
                res.startsWith(KVClientMessage.StatusType.PUT_UPDATE.toString().toLowerCase(Locale.ROOT))) {
            LOGGER.info("Case is put_replica ");
            return "put_replica";
        }

        if (res.startsWith(KVClientMessage.StatusType.DELETE_SUCCESS.toString().toLowerCase(Locale.ROOT))) {
            LOGGER.info("Case is delete_replica ");
            return "delete_replica";
        }

        return null;
    }
}
