package de.tum.i13.client;

import de.tum.i13.shared.ClientConfig;
import de.tum.i13.shared.ConnectionManager.ClientNotificationThread;

import static de.tum.i13.shared.LogSetup.setupLogging;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class KVClient {
	private static final Logger LOGGER = Logger.getLogger(KVClient.class.getName());

	public static void main(String[] args) {
		ClientConfig cfg = ClientConfig.parseCommandlineArgs(args);

		setupLogging(Paths.get("echo-client.log"), Level.ALL);

		CommandSender.checkValidInternetAddress(cfg.serverAddr.getHostString());

		String serverIp = cfg.serverAddr.getHostName();
		int serverPort = cfg.serverAddr.getPort();
		LOGGER.fine("Initial server info host/port: " + serverIp + "/" + serverPort);

		KVStoreClientLibrary kvStore = new KVStoreClientLibraryImpl(serverIp, serverPort);

		try {
			final ServerSocket notificationThreadSocket = new ServerSocket();
			notificationThreadSocket.bind(new InetSocketAddress(cfg.listenaddr, cfg.port));
			ClientNotificationThread clientNotificationThread = new ClientNotificationThread(notificationThreadSocket);
			clientNotificationThread.start();

		} catch (IOException e) {
			e.printStackTrace();
		}

		CLI(kvStore);
	}

	public static void CLI(KVStoreClientLibrary kvStore) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

		while (true) {
			try {
				System.out.print("EchoClient> ");
				if (reader == null) {
					System.out.println("line is null");
					return;
				}
				String line = reader.readLine();

				if (line == null) {
					System.out.println("line is null");
					continue;
				}
				LOGGER.info("Client received command " + line);

				String[] command = line.split(" ");
				switch (command[0]) {
				case "put":
					LOGGER.info("Client requesting put");
					printEchoLine(kvStore.sendPutRequest(line));
					break;
				case "get":
					LOGGER.info("Client requesting get");
					printEchoLine(kvStore.sendGetRequest(line));
					break;
				case "delete":
					LOGGER.info("Client requesting delete");
					printEchoLine(kvStore.sendDeleteRequest(line));
					break;
				case "help":
				case "":
					LOGGER.info("Client printing help");
					printHelp();
					break;
				case "subscribe":
					LOGGER.info("Client requesting subscribe");
					printEchoLine(kvStore.sendSubscribeRequest(line));
					break;
				case "unsubscribe":
					LOGGER.info("Client requesting unsubscribe");
					printEchoLine(kvStore.sendUnsubscribeRequest(line));
					break;
				case "quit":
					LOGGER.info("Client exiting");
					printEchoLine("Application exit!");
					return;
				default:
					LOGGER.info("Client unknown command");
					printEchoLine("Unknown command.");
				}
			} catch (Exception e) {
				LOGGER.throwing("KVClient", "main", e);
				printEchoLine("Error with message: " + e.getMessage());
			}
		}
	}

	private static void printHelp() {
		System.out.println("\tAvailable commands:");
		System.out.println("\t\tconnect <address> <port> - Tries to establish a TCP connection to the server.");
		System.out.println("\t\tdisconnect - Tries to disconnect from the connected server.");
		System.out.println(
				"\t\tsend <message> - Sends a text message to the server according to the communication protocol.");
		System.out.println(
				"\t\tlogLevel <level> - Sets the logger to the specified log level (ALL | DEBUG | INFO | WARN | ERROR | FATAL | OFF)");
		System.out.println("\t\thelp - Displays this help description.");
		System.out
				.println("\t\tquit - Tears down the active connection to the server and exits the program execution.");
	}

	private static void printEchoLine(String msg) {
		String[] parts = msg.split(" ");
		StringBuilder output = new StringBuilder();
		for (String part : parts) {
			if (!Objects.equals(part, "null")) {
				output.append(part).append(" ");
			}
		}
		output.deleteCharAt(output.length() - 1);
		System.out.println("EchoClient> " + output);
	}

}
