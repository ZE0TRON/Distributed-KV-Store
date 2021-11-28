package de.tum.i13.ecs;

import java.util.Locale;

public class ECSMessageImpl implements ECSMessage {
	
	private final String key;
	
	private final String value;

	private final StatusType status;

	
	public ECSMessageImpl(String key, String value, StatusType status) {
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
		return this.status.toString().toLowerCase(Locale.ROOT) + " " + this.key  + " " + this.value;
	}
}
