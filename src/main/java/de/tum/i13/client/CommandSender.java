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

	/**
	 * connects to the server with the given address and port and sends the given
	 * command
	 * 
	 * @param host    the address of the server to be connected
	 * @param port    the port of the server to be connected
	 * @param command the command to be sent to the server
	 * @return response of the server
	 */
	public String sendCommandToServer(String host, int port, String command) throws IOException {
		checkValidInternetAddress(host);

		ActiveConnection activeConnection = buildConnection(host, port);
		if (activeConnection == null)
			return "Could not connect to server!";

		LOGGER.fine("Sending the command: " + command);
		activeConnection.write(command);

		String response = activeConnection.readline();
		LOGGER.fine("Response for the command: " + command + " response: " + response);

		closeConnection(activeConnection);

		return response;
	}

	
	/**
	 * Builds the connection to the server with the given address and port with the
	 * confirmations response of the server for logging and handles the exception
	 * with further information.
	 * 
	 * @param host the address of the server
	 * @param port the port of the server
	 * @return {@code ActiveConnection} that represents this connection
	 * @throws NumberFormatException if the given port number is in a false format
	 * @throws ConnectionException   if an error occurs while connecting to the
	 *                               server
	 * @throws ConnectException      if the server does not accept the connection
	 *                               request
	 */

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

	/**
	 * Connects to the given host and port.
	 * 
	 * @param host the host to be connected.
	 * @param port the port of the host to be connected.
	 * @return {@code ActiveConnection} that represents this connection.
	 * @throws IOException if an error occurs while connecting.
	 */
	private ActiveConnection connect(String host, int port) throws IOException {
		LOGGER.fine("Connecting to host/port: " + host + "/" + port);
		Socket s = new Socket(host, port);

		PrintWriter output = new PrintWriter(new OutputStreamWriter(s.getOutputStream(), Constants.TELNET_ENCODING));
		BufferedReader input = new BufferedReader(new InputStreamReader(s.getInputStream(), Constants.TELNET_ENCODING));

		return new ActiveConnection(s, output, input);
	}

	/**
	 * Closes the given {@code ActiveConnection}
	 * 
	 * @param activeConnection {@code ActiveConnection} to be closed.
	 * @throws ConnectException if an error occurs while disconnecting.
	 */
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

	/**
	 * Check if the given Internet address is valid.
	 * 
	 * @param url address to be checked.
	 * @throws UnknownHostException if an invalid address is given.
	 * 
	 */
	public static void checkValidInternetAddress(String url) {
		try {
			InetAddress.getByName(url);
		} catch (UnknownHostException ex) {
			throw new ConnectionException("Invalid address for connect command!");
		}
	}

}
