package de.tum.i13.client;

public interface KVStoreClientLibrary {

	public String sendGetRequest(String line) throws Exception;

	public String sendPutRequest(String line) throws Exception;

	public String sendDeleteRequest(String line) throws Exception;
}
