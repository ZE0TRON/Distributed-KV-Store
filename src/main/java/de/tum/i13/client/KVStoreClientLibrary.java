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

	public String sendGetRequest(String line) throws Exception {
		String[] parts = line.split(" ");

		if (parts.length != 2)
			return "Error! Invalid command!" + line;
		
		return sendRequest(line);
//
//		CommandSender cs = new CommandSender();
//		KeyRange kr = findCorrectKeyRange(parts[1]);
//		String responce = cs.sendCommandToServer(kr.host, kr.port, line);
//
//		parts = responce.split(" ");
//
//		switch (parts[0]) {
//		case "not_responsible":
//			updateKeyRanges(kr.host, kr.port);
//			return sendGetRequest(line);
//		case "server_stopped":
//			// FIXME implement according to
//			// https://aws.amazon.com/tr/blogs/architecture/exponential-backoff-and-jitter/
//			Thread.sleep(1000);
//			return sendGetRequest(line);
//		case "get_success":
//		case "get_error":
//			return responce;
//		case "server_write_lock":
//			// TODO ??????
//			return responce;
//		default:
//			// Error case
//			return responce;
//		}
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

//		CommandSender cs = new CommandSender();
//		KeyRange kr = findCorrectKeyRange(parts[1]);
//		String responce = cs.sendCommandToServer(kr.host, kr.port, line);
//
//		parts = responce.split(" ");
//
//		switch (parts[0]) {
//		case "not_responsible":
//			updateKeyRanges(kr.host, kr.port);
//			return sendPutRequest(line);
//		case "server_stopped":
//			// FIXME implement according to
//			// https://aws.amazon.com/tr/blogs/architecture/exponential-backoff-and-jitter/
//			Thread.sleep(1000);
//			return sendPutRequest(line);
//		case "put_success":
//		case "put_update":
//		case "put_error":
//			return responce;
//		case "server_write_lock":
//			// TODO ??????
//			return responce;
//		default:
//			// Error case
//			return responce;
//		}
	}

	public String sendDeleteRequest(String line) throws Exception {
		String[] parts = line.split(" ");

		if (parts.length != 2)
			return "Error! Invalid command!" + line;
		
		return sendRequest(line);
//
//		CommandSender cs = new CommandSender();
//		KeyRange kr = findCorrectKeyRange(parts[1]);
//		String responce = cs.sendCommandToServer(kr.host, kr.port, line);
//
//		parts = responce.split(" ");
//
//		switch (parts[0]) {
//		case "not_responsible":
//			updateKeyRanges(kr.host, kr.port);
//			return sendGetRequest(line);
//		case "server_stopped":
//			// FIXME implement according to
//			// https://aws.amazon.com/tr/blogs/architecture/exponential-backoff-and-jitter/
//			Thread.sleep(1000);
//			return sendGetRequest(line);
//		case "delete_success":
//		case "delete_error":
//			return responce;
//		case "server_write_lock":
//			// TODO ??????
//			return responce;
//		default:
//			// Error case
//			return responce;
//		}
	}
	
	private String sendRequest(String line) throws Exception {
		String[] parts = line.split(" ");


		CommandSender cs = new CommandSender();
		KeyRange kr = findCorrectKeyRange(parts[1]);
		String responce = cs.sendCommandToServer(kr.host, kr.port, line);

		parts = responce.split(" ");

		switch (parts[0]) {
		case "not_responsible":
			updateKeyRanges(kr.host, kr.port);
			return sendRequest(line);
		case "server_stopped":
			// FIXME implement according to
			// https://aws.amazon.com/tr/blogs/architecture/exponential-backoff-and-jitter/
			Thread.sleep(1000);
			return sendRequest(line);
			/*
		case "server_write_lock":
			// TODO ??????
			return responce;
		case "delete_success":
		case "delete_error":
			return responce;
			*/
		default:
			return responce;
		}
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
