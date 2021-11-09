package de.tum.i13.server.storageManagment;

public abstract class DataManager {

	/**
	 * Get data
	 * @param key
	 * @return
	 */
	abstract public String get(String key);
	/**
	 * Put data with key
	 * @param key
	 * @param value
	 */
	abstract public void put(String key, String value);

	/**
	 * Delete data
	 * @param key
	 * @return
	 */
	abstract public boolean delete(String key);

}
