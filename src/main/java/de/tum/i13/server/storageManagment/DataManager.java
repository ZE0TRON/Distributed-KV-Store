package de.tum.i13.server.storageManagment;

interface DataManager {

	/**
	 * Get data
	 * 
	 * @param key
	 * @return value for the given key, null if key not found
	 */
	String get(String key);

	/**
	 * Put data with key
	 * 
	 * @param key
	 * @param value
	 * @return true if key value pair is inserted, if value for key is updated, return false
	 */
	boolean put(String key, String value);

	/**
	 * Delete data
	 * 
	 * @param key
	 * @return
	 */
	boolean delete(String key);

}
