package de.tum.i13.client;

import de.tum.i13.shared.ClientConfig;
import de.tum.i13.shared.ConnectionManager.ClientNotificationHandlerThread;
import de.tum.i13.shared.Util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import static de.tum.i13.shared.LogSetup.setupLogging;

public class KVPerformanceTest1 {
	private static final Logger LOGGER = Logger.getLogger(KVPerformanceTest1.class.getName());
	public static int LISTEN_PORT;
	public static boolean IS_CONTAINER;
	public static void main(String[] args) {
		ClientConfig cfg = ClientConfig.parseCommandlineArgs(args);

		setupLogging(Paths.get("echo-client.log"), Level.ALL);

		String serverIp = cfg.serverAddr;
		int serverPort = cfg.serverPort;
		IS_CONTAINER = cfg.isContainer;
		LISTEN_PORT = cfg.port;
		if(LISTEN_PORT == 0) {
			LISTEN_PORT = Util.getRandomAvailablePort();
		}
		LOGGER.info("Initial server info host/port: " + serverIp + "/" + serverPort);

		KVStoreClientLibrary kvStore = new KVStoreClientLibraryImpl(serverIp, serverPort);

		ClientNotificationHandlerThread clientNotificationHandlerThread = new ClientNotificationHandlerThread(cfg.listenaddr, LISTEN_PORT);
		clientNotificationHandlerThread.start();
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			System.out.println("Closing thread main KVClient. Shutdown procedure has been started.");
			try {
				shutdownProcedure();
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}));
		scenario(kvStore);
	}

	public static void shutdownProcedure() throws IOException, InterruptedException {
		Thread.sleep(750);
		ClientNotificationHandlerThread.kill();
	}

	public static void scenario(KVStoreClientLibrary kvStore) {
		try {
			String response = kvStore.sendPutRequest("put abc 123");
			for (int i =0; i<10000; i++) {
				// send some random requests gets etc
			}
		} catch (Exception e) {
			System.out.println("Exception caught " + e.getMessage());
		}
	}

}
