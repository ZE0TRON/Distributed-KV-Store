package de.tum.i13.server;

import de.tum.i13.server.kv.*;
import de.tum.i13.shared.ConnectionManager.ConnectionThread;
import de.tum.i13.shared.ConnectionManager.EcsConnectionThread;
import de.tum.i13.shared.ConnectionManager.HeartbeatThread;
import de.tum.i13.shared.ServerConfig;

import static de.tum.i13.shared.Constants.HEARTBEAT_PORT;
import static de.tum.i13.shared.LogSetup.setupLogging;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import de.tum.i13.server.storageManagment.CacheManagerFactory;
import de.tum.i13.shared.Util;

/**
 * Created by chris on 09.01.15.
 */
public class Main {
    public static String serverIp;
    public static int port;

    public static void main(String[] args) throws IOException {
        ServerConfig cfg = ServerConfig.parseCommandlineArgs(args);  //Do not change this
        setupLogging(cfg.logfile, cfg.logLevel);
        serverIp = cfg.listenaddr;
        port = cfg.port;
        if (port == 0) {
           port = Util.getRandomAvailablePort();
        }

        final ServerSocket kvServerSocket = new ServerSocket();
        kvServerSocket.bind(new InetSocketAddress(cfg.listenaddr, cfg.port));

        try {
            Persist.init(cfg.dataDir);
            CacheManagerFactory.create(cfg.cacheSize, cfg.cacheDisplacementStrategy);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        CommandProcessor.serverState = ServerState.SERVER_STOPPED;
        CommandProcessor logic = new CommandProcessor(KVStoreImpl.getInstance());
        KVCommandProcessor kvTransferLogic = new KVCommandProcessor(new KVTransferService(KVStoreImpl.getInstance()));

        System.out.println("Ecs Addr : " + cfg.ecsAddr + " Ecs Port : " + cfg.ecsPort);
        Socket ecsSocket = new Socket(cfg.ecsAddr, cfg.ecsPort);
        EcsConnectionThread ecsThread = new EcsConnectionThread(logic, kvTransferLogic, ecsSocket);
        ecsThread.start();

        final ServerSocket ecsHeartbeatSocket = new ServerSocket();
        ecsHeartbeatSocket.bind(new InetSocketAddress(cfg.listenaddr, HEARTBEAT_PORT));
        HeartbeatThread heartbeatThread = new HeartbeatThread(ecsHeartbeatSocket);
        heartbeatThread.start();

        // Graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Closing thread main KVServer. Shutdown procedure has been started.");
            try {
                shutdownProcedure(kvServerSocket, ecsHeartbeatSocket, ecsThread);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }));


        while (true) {
            Socket clientSocket = kvServerSocket.accept();
            // When client connection comes through, start a new Thread for this client
            System.out.println("Client connected");
            Thread th = new ConnectionThread(logic, kvTransferLogic, clientSocket,  "Connection established.");
            th.start();
        }
    }

    public static void shutdownProcedure(ServerSocket kvServerSocket, ServerSocket ecsHeartbeatSocket, EcsConnectionThread ecsConnectionThread) throws IOException, InterruptedException {
        String address = Main.serverIp;
        String port = String.valueOf(Main.port);
        String payload ="shutdown " + address + " " +  port;
        EcsConnectionThread.ECSConnection.send(payload);
        Thread.sleep(4000);
        kvServerSocket.close();
        ecsHeartbeatSocket.close();
        ecsConnectionThread.kill();
    }
}
