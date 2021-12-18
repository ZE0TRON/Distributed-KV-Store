package de.tum.i13.client;

import static de.tum.i13.shared.LogSetup.setupLogging;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class KVClient {
	private static Logger LOGGER = Logger.getLogger(KVClient.class.getName());

	public static void main(String[] args) {
		setupLogging(Paths.get("echo-client.log"), Level.ALL);

		String host = args[0];
		CommandSender.checkValidInternetAddress(host);
		int port = Integer.parseInt(args[1]);

		LOGGER.fine("Initial server info host/post: " + host + "/" + port);

		KVStoreClientLibrary kvStore = new KVStoreClientLibraryImpl(host, port, new CommandSender());

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
					printEchoLine(kvStore.sendPutRequest(line));
					LOGGER.info("Client requesting put");
					break;
				case "get":
					printEchoLine(kvStore.sendGetRequest(line));
					LOGGER.info("Client requesting get");
					break;
				case "delete":
					printEchoLine(kvStore.sendDeleteRequest(line));
					LOGGER.info("Client requesting delete");
					break;
				case "help":
				case "":
					printHelp();
					LOGGER.info("Client printing help");
					break;
				case "quit":
					printEchoLine("Application exit!");
					LOGGER.info("Client exiting");
					return;
				default:
					printEchoLine("Unknown command.");
					LOGGER.info("Client unknown command");
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
		for (int i = 0; i < parts.length; i++) {
			String part = parts[i];

			if (!Objects.equals(part, "null")) {
				output.append(part).append(" ");
			}
		}
		output.deleteCharAt(output.length() - 1);
		System.out.println("EchoClient> " + output);
	}

}
