package de.tum.i13.server.storageManagment;

import de.tum.i13.server.kv.KVItem;


/**
 * All classes representing a manager, which controls and executes operations on
 * data, should implement this interface.
 */
public interface DataManager {

	/**
	 * Searches the value for the given key and then gets it.
	 * 
	 * @param key the key, whose value we want to acquire.
	 * @return KVItem, null if this key is not found.
	 */
	KVItem get(String key) throws Exception;

	/**
	 * Puts the pair of key and value into the storage.
	 * 
	 * @param kvItem, the key, value pair.
	 * @return true if this key and value pair is inserted, if the value for this
	 *         key is updated, return false.
	 */
	PersistType put(KVItem kvItem) throws Exception;

	/**
	 * Deletes the data with this key.
	 *
	 * @param key, the key of the data we want to delete.
	 * @return true
	 */
	PersistType delete(String key) throws Exception;

}
