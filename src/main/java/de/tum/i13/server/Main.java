package de.tum.i13.server;

import de.tum.i13.server.kv.KVPersist;
import de.tum.i13.server.thread.ConnectionHandleThread;
import de.tum.i13.shared.CommandProcessor;
import de.tum.i13.shared.Config;
import static de.tum.i13.shared.Config.parseCommandlineArgs;
import static de.tum.i13.shared.LogSetup.setupLogging;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import de.tum.i13.server.kv.KVCommandProcessor;
import de.tum.i13.server.kv.KVStoreImpl;
import de.tum.i13.server.storageManagment.CacheManagerFactory;

/**
 * Created by chris on 09.01.15.
 */
public class Main {

    public static void main(String[] args) throws IOException {
        Config cfg = parseCommandlineArgs(args);  //Do not change this
        setupLogging(cfg.logfile, cfg.logLevel);

        final ServerSocket serverSocket = new ServerSocket();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Closing thread per connection kv server");
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));

        //bind to localhost only
        serverSocket.bind(new InetSocketAddress(cfg.listenaddr, cfg.port));

        try {
            KVPersist.init(cfg.dataDir);
            CacheManagerFactory.create(cfg.cacheSize, cfg.cacheDisplacementStrategy);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        // If you use multithreading you need locking
        CommandProcessor logic = new KVCommandProcessor(new KVStoreImpl());



        // TODO checkout help thing
        while (true) {
            Socket clientSocket = serverSocket.accept();
            //When we accept a connection, we start a new Thread for this connection
            System.out.println("Client connected");
            Thread th = new ConnectionHandleThread(logic, clientSocket);
            th.start();
        }
    }
}
