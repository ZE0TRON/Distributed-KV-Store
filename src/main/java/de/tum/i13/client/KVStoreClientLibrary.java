package de.tum.i13.client;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import javax.xml.bind.DatatypeConverter;

public class KVStoreClientLibrary {

	private ArrayList<KeyRange> metaData;

	public KVStoreClientLibrary(String host, int port) {
		metaData = new ArrayList<>();
		metaData.add(new KeyRange(null, null, host, port)); // FIXME eksik
	}

	public String sendGetRequest(String line) throws Exception {
		String[] parts = line.split(" ");

		if (parts.length != 2)
			return "Error! Invalid command!" + line;

		return sendRequest(line);
	}

	// This method replicates a lot of lines of the sendGetRequest method, to avoid
	// line duplication we can put handle both
	// of the requests in one func but it didn't felt like good practice (we should
	// keep them separate I believe). Maybe
	// we can generate helper funcs to check inputs or read/write to buffers. Would
	// like feedbacks here. -cenk
	public String sendPutRequest(String line) throws Exception {
		String[] parts = line.split(" ");

		if (parts.length != 3)
			return "Error! Invalid command!" + line;

		return sendRequest(line);

//		}
	}

	public String sendDeleteRequest(String line) throws Exception {
		String[] parts = line.split(" ");

		if (parts.length != 2)
			return "Error! Invalid command!" + line;

		return sendRequest(line);
	}

	private String sendRequest(String line) throws Exception {
		String[] parts = line.split(" ");

		CommandSender cs = new CommandSender();
		KeyRange kr = findCorrectKeyRange(parts[1]);
		String response = cs.sendCommandToServer(kr.host, kr.port, line);

		parts = response.split(" ");

		switch (parts[0]) {
		case "not_responsible":
			updateKeyRanges(kr.host, kr.port);
			return sendRequest(line);
		case "server_stopped":
			// FIXME implement according to
			// https://aws.amazon.com/tr/blogs/architecture/exponential-backoff-and-jitter/
			Thread.sleep(1000);
			return sendRequest(line);

//		case "server_write_lock":
//			// TODO ??????
//			return response;
//		case "delete_success":
//		case "delete_error":
//			return response;

		// yeni key-range daðýlýmý yapýlýrken
		default:
			return response;
		}
	}

	private void updateKeyRanges(String host, int port) throws IOException {
		CommandSender cs = new CommandSender();
		String response = cs.sendCommandToServer(host, port, "keyrange");
		String[] keyRanges = response.split(";");
		metaData = new ArrayList<>();
		for (String v : keyRanges) {
			String[] parts = v.split(",");
			if (parts.length != 4)
				throw new RuntimeException("Invalid key range");
			metaData.add(new KeyRange(parts[0], parts[1], parts[2], Integer.parseInt(parts[3])));
		}

	}

	private KeyRange findCorrectKeyRange(String key) throws NoSuchAlgorithmException {
		if (metaData.size() == 1)
			return metaData.get(0);

		String hashCode = findHash(key);
		for (KeyRange keyRange : metaData) {
			if (keyRange.from.compareTo(hashCode) > 0 && keyRange.to.compareTo(hashCode) <= 0)
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
