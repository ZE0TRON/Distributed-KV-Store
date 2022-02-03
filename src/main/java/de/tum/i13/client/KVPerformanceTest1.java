package de.tum.i13.client;

import de.tum.i13.shared.ClientConfig;
import de.tum.i13.shared.ConnectionManager.ClientNotificationHandlerThread;
import de.tum.i13.shared.Util;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import static de.tum.i13.shared.LogSetup.setupLogging;

public class KVPerformanceTest1 {
	private static final Logger LOGGER = Logger.getLogger(KVPerformanceTest1.class.getName());
	public static int LISTEN_PORT;
	public static boolean IS_CONTAINER;
	public static void main(String[] args) {
		System.out.println(randomString());
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
			// Get keyrange if not implemented in the put
			// Time start for overhead if keyrange request is sent
			String response = kvStore.sendPutRequest("put abc 123");
			// Time stop for overhead if keyrange request is sent
			String key;
			String value;
			for (int i =0; i<10000; i++) {
				key = randomString();
				value = randomString();

				//Time start for requests
				kvStore.sendPutRequest("put" + key  + " " + value);
				// time stop for requests
				key = randomString();
				value = randomString();
				//Time start for requests
				kvStore.sendGetRequest("get" + key + " " + value);
				// time stop for requests
				// if doesn't work
				//Thread.sleep(1);
			}
		} catch (Exception e) {
			System.out.println("Exception caught " + e.getMessage());
		}
	}
	// Taken from https://www.baeldung.com/java-random-string
	public static String randomString() {
		int leftLimit = 97; // letter 'a'
		int rightLimit = 122; // letter 'z'
		int targetStringLength = 10;
		Random random = new Random();

		String generatedString = random.ints(leftLimit, rightLimit + 1)
				.limit(targetStringLength)
				.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
				.toString();

		return  generatedString;
	}
}
