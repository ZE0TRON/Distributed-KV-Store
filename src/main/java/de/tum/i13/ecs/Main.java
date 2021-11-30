package de.tum.i13.ecs;

import de.tum.i13.ecs.cs.ECS;
import de.tum.i13.shared.ConnectionManager.ConnectionHandleThread;
import de.tum.i13.shared.CommandProcessor;
import de.tum.i13.shared.ECSConfig;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import static de.tum.i13.shared.ECSConfig.parseCommandlineArgs;
import static de.tum.i13.shared.LogSetup.setupLogging;

public class Main {
    public static void main(String[] args) throws IOException {
        // TODO setup config parser for ECS
        ECSConfig cfg = parseCommandlineArgs(args);  //Do not change this
        // TODO logging or not ?
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
            // TODO init services
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        // If you use multithreading you need locking
        CommandProcessor logic = new ECSCommandProcessor(ECS.getInstance());
        // TODO checkout help thing
        while (true) {
            Socket clientSocket = serverSocket.accept();
            //When we accept a connection, we start a new Thread for this connection
            System.out.println("KV Server connected");
            Thread th = new ConnectionHandleThread(logic, clientSocket);
            th.start();
        }
    }
}
