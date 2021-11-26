package de.tum.i13.client;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import javax.xml.bind.DatatypeConverter;

public class KVStoreClientLibrary {

	private ArrayList<KeyRange> keyRangeList;

	public KVStoreClientLibrary(String host, int port) {
		keyRangeList = new ArrayList<>();
		keyRangeList.add(new KeyRange(null, null, host, port)); // FIXME eksik
	}

	// FIXME improve error handling
	public String sendGetRequest(String line) throws NoSuchAlgorithmException {

		int firstSpace = line.indexOf(" ");
		if (firstSpace == -1 || firstSpace + 1 >= line.length())
			return "Error! Nothing to send!";

		try {
			CommandSender cs = new CommandSender();
			// FIXME find correct server according to hash code
			String[] parts = line.split(" ");
			KeyRange kr = findCorrectKeyRange(parts[1]);
			String responce = cs.sendCommandToServer(kr.host, kr.port, line);

			parts = responce.split(" ");

			switch (parts[0]) {
			case "put_success":
			case "put_update":
			case "put_error":
				return responce;
			case "not_responsible":
				updateKeyRanges(kr.host, kr.port);
				return sendGetRequest(line);
			case "server_stopped":
				// TODO implement according to
				// https://aws.amazon.com/tr/blogs/architecture/exponential-backoff-and-jitter/
			case "server_write_lock":
				// TODO ??????
			}

			return null;
		} catch (IOException e) {
			return "Error! Not connected!";
		}
	}

	private KeyRange findCorrectKeyRange(String key) throws NoSuchAlgorithmException {
		if (keyRangeList.size() == 1)
			return keyRangeList.get(0);

		String hashCode = findHash(key);
		for (KeyRange keyRange : keyRangeList) {
			if (keyRange.from.compareTo(hashCode) >= 0 && keyRange.to.compareTo(hashCode) <= 0)
				return keyRange;
		}
		return null;
	}

	private String findHash(String key) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("MD5");
		md.update(key.getBytes());

		byte[] digest = md.digest();
		return DatatypeConverter.printHexBinary(digest).toUpperCase();

	}

	private void updateKeyRanges(String host, int port) throws IOException {
		CommandSender cs = new CommandSender();
		String responce = cs.sendCommandToServer(host, port, "keyrange");
		String[] keyRanges = responce.split(";");
		keyRangeList = new ArrayList<>();
		for (String v : keyRanges) {
			String[] parts = v.split(",");
			if (parts.length != 4)
				throw new RuntimeException("Invalid key range");
			keyRangeList.add(new KeyRange(parts[0], parts[1], parts[2], Integer.parseInt(parts[3])));
		}

	}

	// This method replicates a lot of lines of the sendGetRequest method, to avoid
	// line duplication we can put handle both
	// of the requests in one func but it didn't felt like good practice (we should
	// keep them separate I believe). Maybe
	// we can generate helper funcs to check inputs or read/write to buffers. Would
	// like feedbacks here. -cenk
	public String sendPutRequest(String[] command, String line) {
		return null;
		/*
		 * if (activeConnection == null) return "Error! Not connected!";
		 * 
		 * int firstSpace = line.indexOf(" "); if (command.length < 2 || firstSpace ==
		 * -1) return "Error! A put request must be like this: put <key> <value>.";
		 * 
		 * activeConnection.write(line);
		 * 
		 * try { return activeConnection.readline(); } catch (IOException e) { return
		 * "Error! Not connected!"; }
		 */
	}

	private class KeyRange {
		String from;
		String to;
		String host;
		int port;

		public KeyRange(String from, String to, String host, int port) {
			this.from = from;
			this.to = to;
			this.host = host;
			this.port = port;
		}

	}

}
