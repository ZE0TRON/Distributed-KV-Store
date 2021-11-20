package de.tum.i13.server.kv;

public class KVMessageImpl implements KVMessage {
	
	private final String key;
	
	private final String value;

	private final StatusType status;

	
	public KVMessageImpl(String key, String value, StatusType status) {
		this.key = key;
		this.value = value;
		this.status = status;
	}

	@Override
	public String getKey() {
		return key;
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public StatusType getStatus() {
		return status;
	}

}
