package de.tum.i13.server.storageManagment;

import de.tum.i13.server.kv.PersistItem;

/**
 * All classes representing a manager, which controls and executes operations on
 * data, should implement this interface.
 */
public interface DataManager {

	/**
	 * Searches the value for the given key and then gets it.
	 * 
	 * @param key the key, whose item we want to acquire.
	 * @return PersistItem, null if this key is not found.
	 */
	PersistItem get(String key);

	/**
	 * Puts the given key-value item.
	 * 
	 * @param persistItem the key-value item to be put.
	 * @return PersistType.UPDATE if an item with the same key already exists,
	 *         PersistType.INSERT otherwise.
	 */
	PersistType put(PersistItem persistItem);

	/**
	 * Deletes the item with the given key.
	 *
	 * @param key the key of the item to be deleted.
	 * @return PersistType.DELETE after delete operation is completed.
	 */
	PersistType delete(String key) throws Exception;

}
