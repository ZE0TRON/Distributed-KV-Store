package de.tum.i13.client;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import de.tum.i13.shared.keyring.ConsistentHashingService;
import de.tum.i13.shared.keyring.HashService;

public class KVStoreClientLibraryImpl implements KVStoreClientLibrary {
	
	private CommandSender cs = new CommandSender();

	private ArrayList<KeyRange> metaData;

	private HashService hashService;
	private static final String HASHING_ALGORITHM = "md5";
	private static final int MAX_SLEEP_IN_MILLI_SECOND = 1000;
	private static final int SLEEP_BASE_IN_MILLI_SECOND = 10;

	/**
	 * Constructs a {@code KVStoreClientLibraryImpl} with the given address and port
	 * and initializes its meta data list.
	 * 
	 * @param host the address of this {@code KVStoreClientLibraryImpl}
	 * @param port the port of this {@code KVStoreClientLibraryImpl}
	 * @param commandSender 
	 */
	public KVStoreClientLibraryImpl(String host, int port, CommandSender commandSender) {
		this.cs = commandSender;
		this.metaData = new ArrayList<>();
		this.metaData.add(new KeyRange(null, null, host, port));

		try {
			this.hashService = new ConsistentHashingService(HASHING_ALGORITHM);
		} catch (NoSuchAlgorithmException e) {
			// TODO Log here
			e.printStackTrace();
		}
	}

	/**
	 * Sends the given get request to the server if it is valid request.
	 * 
	 * @param line the request to be sent.
	 * @return the response of the server if the given request line is a valid get
	 *         request, error message otherwise.
	 */
	public String sendGetRequest(String line) throws Exception {
		String[] parts = line.split(" ");

		if (parts.length != 2)
			return "Error! Invalid command!" + line;

		return sendRequest(line);
	}

	/**
	 * Sends the given put request to the server if it is valid request.
	 * 
	 * @param line the request to be sent.
	 * @return the response of the server if the given request line is a valid put
	 *         request, error message otherwise.
	 */
	public String sendPutRequest(String line) throws Exception {
		String[] parts = line.split(" ");

		if (parts.length != 3)
			return "Error! Invalid command!" + line;

		return sendRequest(line);

	}

	/**
	 * Sends the given delete request to the server if it is valid request.
	 * 
	 * @param line the request to be sent.
	 * @return the response of the server if the given request line is a valid
	 *         delete request, error message otherwise.
	 */
	public String sendDeleteRequest(String line) throws Exception {
		String[] parts = line.split(" ");

		if (parts.length != 2)
			return "Error! Invalid command!" + line;

		return sendRequest(line);
	}

	/**
	 * Realizes the parts in sending get, put and delete requests.
	 * 
	 * @param line the request to be sent.
	 * @return the response of the server if the given request line is a valid
	 *         request, error message otherwise.
	 * @throws Exception
	 */

	private String sendRequest(String line) throws Exception {
		return sendRequest(line, 0);
	}

	private String sendRequest(String line, int attempt) throws Exception {
		String[] parts = line.split(" ");

		
		KeyRange kr = findCorrectKeyRange(parts[1]);
		String response = cs.sendCommandToServer(kr.host, kr.port, line);

		parts = response.split(" ");

		switch (parts[0]) {
		case "not_responsible":
			updateKeyRanges(kr.host, kr.port);
			return sendRequest(line);
		case "server_stopped":
			sleepBackoffAndJitter(attempt++);
			return sendRequest(line);

//		case "server_write_lock":
//		case "get_success":
//		case "get_error":
//		case "put_success":
//		case "put_update":
//		case "put_error":
//		case "delete_success":
//		case "delete_error":
		default:
			return response;
		}
	}

	/**
	 * Causes the currently executing thread to sleep for the number of milliseconds
	 * specified by "Full Jitter" algorithm explained in
	 * https://aws.amazon.com/tr/blogs/architecture/exponential-backoff-and-jitter/
	 * 
	 * @param attempt number of attempts made by the client
	 * @throws InterruptedException if any thread has interrupted the current
	 *                              thread.
	 */
	private void sleepBackoffAndJitter(int attempt) throws InterruptedException {
		int sleep = Math.min(MAX_SLEEP_IN_MILLI_SECOND, SLEEP_BASE_IN_MILLI_SECOND * (int) Math.pow(2, attempt));
		sleep = (int) (Math.random() * sleep);
		Thread.sleep(sleep);
	}

	/**
	 * Sends the "keyrange" command to the server and updates the key ranges
	 * according to the response of the server.
	 * 
	 * @param host the address of the server.
	 * @param port the port of the server.
	 * @throws IOException      if an error occurs while sending the "keyrange"
	 *                          command to the server.
	 * @throws RuntimeException if the provided key ranges in the response of the
	 *                          server are not in a valid form.
	 */
	public void updateKeyRanges(String host, int port) throws IOException {
		String response = cs.sendCommandToServer(host, port, "keyrange");
		int index = response.indexOf("keyrange_success");
		if (index == -1) {
			if (response.contains("server_stopped")) {
				// Do nothing. next request can handle the situation
				return; 
			} else {
				throw new RuntimeException("Invalid key range: " + response);
			}
		}
		String[] keyRanges = response.substring(index + "keyrange_success".length() + 1).split(";");
		metaData = new ArrayList<>();
		for (String v : keyRanges) {
			String[] parts = v.split(",");
			if (parts.length != 3 || parts[2].indexOf(":") == -1)
				throw new RuntimeException("Invalid key range: " + v);
			
			String from = parts[0];
			String to = parts[1];
			index = parts[2].indexOf(":");
			String serverIP = parts[2].substring(0, index);
			int serverPort = Integer.parseInt(parts[2].substring(index + 1));
			
			metaData.add(new KeyRange(from, to, serverIP, serverPort));
		}

	}

	/**
	 * finds the key range that the given key belongs to.
	 * 
	 * @param key the key whose key range is searched for.
	 * @return {@code KeyRange} that the given key belongs to.
	 * @throws NoSuchAlgorithmException if an error occurs while searching for the
	 *                                  hash code of the key.
	 */
	public KeyRange findCorrectKeyRange(String key) throws NoSuchAlgorithmException {
		if (metaData.size() == 1)
			return metaData.get(0);

		String hashCode = findHash(key);
		for (KeyRange keyRange : metaData) {
			// Wrap around
			if (keyRange.from.compareTo(keyRange.to) > 0) {
				if (keyRange.from.compareTo(hashCode) < 0 || keyRange.to.compareTo(hashCode) > 0) {
					return keyRange;
				}
			} else if (keyRange.from.compareTo(hashCode) > 0 && keyRange.to.compareTo(hashCode) <= 0) {
				return keyRange;
			}
		}
		return null;
	}

	/**
	 * finds the hash code of the given key.
	 * 
	 * @param key the key whose key hash code is searched for.
	 * @return the hash code of the given key as String.
	 * @throws NoSuchAlgorithmException if the requested hash algorithm is not
	 *                                  available.
	 */
	public String findHash(String key) throws NoSuchAlgorithmException {
		return ConsistentHashingService.byteHashToStringHash(hashService.hash(key));
	}

}
