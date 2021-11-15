package de.tum.i13.server.storageManagment;

/**
 * All classes representing a manager, which controls and executes operations on
 * data, should implement this interface.
 */
interface DataManager {

	/**
	 * Searches the value for the given key and then gets it.
	 * 
	 * @param key the key, whose value we want to acquire.
	 * @return value for this key, null if this key is not found.
	 */
	String get(String key);

	/**
	 * Puts the pair of key and value into the storage.
	 * 
	 * @param key,   the key of this pair.
	 * @param value, the value of this pair.
	 * @return true if this key and value pair is inserted, if the value for this
	 *         key is updated, return false.
	 */
	boolean put(String key, String value);

	/**
	 * Deletes the data with this key.
	 *
	 * @param key, the key of the data we want to delete.
	 * @return true
	 */
	boolean delete(String key);

}
