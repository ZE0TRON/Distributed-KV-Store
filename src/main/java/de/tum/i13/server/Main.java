package de.tum.i13.server;

import de.tum.i13.server.kv.*;
import de.tum.i13.shared.ConnectionManager.ClientConnectionThread;
import de.tum.i13.shared.ConnectionManager.EcsConnectionThread;
import de.tum.i13.shared.ServerConfig;
import static de.tum.i13.shared.LogSetup.setupLogging;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import de.tum.i13.server.kv.CommandProcessor;
import de.tum.i13.server.kv.KVStoreImpl;
import de.tum.i13.server.storageManagment.CacheManagerFactory;

/**
 * Created by chris on 09.01.15.
 */
public class Main {

    public static void shutdownProcedure(ServerSocket kvServerSocket, Socket ecsSocket) throws IOException {
        // TODO: handout keys here
        kvServerSocket.close();
        ecsSocket.close();
    }

    public static void main(String[] args) throws IOException {
        ServerConfig cfg = ServerConfig.parseCommandlineArgs(args);  //Do not change this
        setupLogging(cfg.logfile, cfg.logLevel);

        final ServerSocket kvServerSocket = new ServerSocket();
        kvServerSocket.bind(new InetSocketAddress(cfg.listenaddr, cfg.port));

        try {
            Persist.init(cfg.dataDir);
            CacheManagerFactory.create(cfg.cacheSize, cfg.cacheDisplacementStrategy);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        CommandProcessor logic = new CommandProcessor(new KVStoreImpl());

        try {
            // Start ECS Thread
            Socket ecsSocket = new Socket(cfg.bootstrap.getAddress(), cfg.bootstrap.getPort());
            Thread ecsThread = new EcsConnectionThread(logic, ecsSocket);
            ecsThread.start();
//
//            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//                System.out.println("Closing thread main kv server");
//                try {
//                    shutdownProcedure(kvServerSocket, ecsSocket);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }));
        } catch (Exception e){
            e.printStackTrace();
        }

        // Command Processorlari ayir(eminsen)

        // Yeni Thread ac
        // Threadin icinde whilela don kvserver bekle
        // Kvserver connection gelicne yeni threadde handle et
        // key aktarimi bitince connection kapaninca threadi kapa

        // Graceful shutdown here
        while (true) {
            Socket clientSocket = kvServerSocket.accept();
            // When client connection comes through, start a new Thread for this client
            System.out.println("Client connected");
            Thread th = new ClientConnectionThread(logic, clientSocket);
            th.start();
        }
    }
}
