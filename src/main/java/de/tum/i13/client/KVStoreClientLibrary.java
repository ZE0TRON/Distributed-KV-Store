package de.tum.i13.client;

public interface KVStoreClientLibrary {

	String sendGetRequest(String line) throws Exception;

	String sendPutRequest(String line) throws Exception;

	String sendDeleteRequest(String line) throws Exception;
}
