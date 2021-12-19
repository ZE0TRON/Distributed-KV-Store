package de.tum.i13.server;

import de.tum.i13.ecs.cs.ECS;
import de.tum.i13.server.kv.*;
import de.tum.i13.shared.ConnectionManager.ConnectionManager;
import de.tum.i13.shared.ConnectionManager.ConnectionThread;
import de.tum.i13.shared.ConnectionManager.EcsConnectionThread;
import de.tum.i13.shared.ServerConfig;
import static de.tum.i13.shared.LogSetup.setupLogging;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import de.tum.i13.server.storageManagment.CacheManagerFactory;

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
        CommandProcessor logic = new CommandProcessor(new KVStoreImpl());
        KVCommandProcessor kvTransferLogic = new KVCommandProcessor(new KVTransferService(new KVStoreImpl()));

        Socket ecsSocket = new Socket(cfg.bootstrap.getAddress(), cfg.bootstrap.getPort());
        EcsConnectionThread ecsThread = new EcsConnectionThread(logic, kvTransferLogic, ecsSocket);
        ecsThread.start();

        // Graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Closing thread main KVServer. Shutdown procedure has been started.");
            try {
                shutdownProcedure(kvServerSocket, ecsSocket, ecsThread);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));


        while (true) {
            Socket clientSocket = kvServerSocket.accept();
            // When client connection comes through, start a new Thread for this client
            System.out.println("Client connected");
            Thread th = new ConnectionThread(logic, kvTransferLogic, clientSocket, true);
            th.start();
        }
    }

    public static void shutdownProcedure(ServerSocket kvServerSocket, Socket ecsSocket, EcsConnectionThread ecsThread) throws IOException {
        String address = Main.serverIp;
        String port = String.valueOf(Main.port);
        String payload ="shutdown " + address + " " +  port;
        EcsConnectionThread.ECSConnection.send(payload);
        while (!ConnectionThread.CanShutdown){}
        ecsThread.cancel();
        kvServerSocket.close();
    }
}
