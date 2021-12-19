package de.tum.i13.server.kv;

import java.util.Locale;

public class KVClientMessageImpl implements KVClientMessage {
	
	private final String key;
	
	private final String value;

	private final StatusType status;

	
	public KVClientMessageImpl(String key, String value, StatusType status) {
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

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.status.toString().toLowerCase(Locale.ROOT));
		sb.append(" ");
		if(this.key != null) {
			sb.append(this.key);
			sb.append(" ");
		}
		if(this.value != null) {
			sb.append(this.value);
			sb.append(" ");
		}
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}
}
