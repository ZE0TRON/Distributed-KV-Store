package de.tum.i13.server.storageManagment;

interface DataManager {

	/**
	 * Get data
	 * 
	 * @param key
	 * @return
	 */
	String get(String key);

	/**
	 * Put data with key
	 * 
	 * @param key
	 * @param value
	 */
	void put(String key, String value);

	/**
	 * Delete data
	 * 
	 * @param key
	 * @return
	 */
	boolean delete(String key);

}
