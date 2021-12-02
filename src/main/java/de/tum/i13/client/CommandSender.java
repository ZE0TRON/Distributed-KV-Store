package de.tum.i13.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Logger;

import de.tum.i13.client.exception.ConnectionException;
import de.tum.i13.shared.Constants;

public class CommandSender {

	private static Logger LOGGER = Logger.getLogger(CommandSender.class.getName());

	public String sendCommandToServer(String host, int port, String command) throws IOException {
		checkValidInternetAddress(host);

		ActiveConnection activeConnection = buildConnection(host, port);
		if (activeConnection == null)
			return "Could not connect to server!";

		LOGGER.fine("Sending the command: " + command);
		activeConnection.write(command);

		String responce = activeConnection.readline();
		LOGGER.fine("Responce for the command: " + command + " responce: " + responce);

		closeConnection(activeConnection);

		return responce;
	}

	private ActiveConnection buildConnection(String host, int port) {
		try {
			ActiveConnection activeConnection = connect(host, port);

			String confirmation = activeConnection.readline();

			LOGGER.fine(confirmation + " with " + activeConnection.getInfo() + ".");

			return activeConnection;

		} catch (NumberFormatException e) {
			LOGGER.fine("Invalid port number for connect command!");
		} catch (ConnectionException e) {
			LOGGER.fine(e.getMessage());
		} catch (ConnectException e) {
			LOGGER.fine("Server is unreachable, connection refused.");
		} catch (Exception e) {
			LOGGER.fine("Could not connect to server!");
		}
		return null;
	}

	private ActiveConnection connect(String host, int port) throws IOException {
		LOGGER.fine("Connectiong to host/post: " + host + "/" + port);
		Socket s = new Socket(host, port);

		PrintWriter output = new PrintWriter(new OutputStreamWriter(s.getOutputStream(), Constants.TELNET_ENCODING));
		BufferedReader input = new BufferedReader(new InputStreamReader(s.getInputStream(), Constants.TELNET_ENCODING));

		return new ActiveConnection(s, output, input);
	}

	private void closeConnection(ActiveConnection activeConnection) {
		if (activeConnection == null || !activeConnection.isSocketInitiated() || !activeConnection.isSocketConnected()
				|| activeConnection.isSocketClosed()) {
			LOGGER.fine("Not connected.");
		} else {
			try {
				String info = activeConnection.getInfo();
				activeConnection.close();
				LOGGER.fine("Connection with " + info + " has been terminated.");
			} catch (IOException e) {
				throw new ConnectionException("Error while disconnecting ", e);
			}
		}
	}

	public static void checkValidInternetAddress(String url) {
		try {
			InetAddress.getByName(url);
		} catch (UnknownHostException ex) {
			throw new ConnectionException("Invalid address for connect command!");
		}
	}

}
