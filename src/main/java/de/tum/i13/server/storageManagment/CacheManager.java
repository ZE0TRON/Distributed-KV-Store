package de.tum.i13.server.storageManagment;

import de.tum.i13.server.kv.PersistItem;

import java.util.HashMap;
import java.util.logging.Logger;

public abstract class CacheManager implements DataManager {

	private static final Logger LOGGER = Logger.getLogger(CacheManager.class.getName());

	protected static CacheManager instance;

	protected int cacheSize;

	protected HashMap<String, Value> map;

	protected CacheManager() {
		map = new HashMap<>();
	}

	/**
	 * Gets the instance of this {@code CacheManager}.
	 * 
	 * @return this {@code CacheManager} if it is already initialized before.
	 * @throws RuntimeException if this {@code CacheManager} is not already
	 *                          initialized.
	 */
	public static CacheManager getInstance() {
		if (!isInitialized()) {
			throw new RuntimeException("Initialize CacheManager first!");
		}

		return instance;
	}

	/**
	 * Checks if this {@code CacheManager} is already initialized.
	 * 
	 * @return true if initialized, false otherwise.
	 */
	public static boolean isInitialized() {
		return instance != null;
	}

	/**
	 * Gets the value for the given key.
	 * 
	 * @param key the key of the wanted item.
	 * @return PersistItem with the given key, null if the key is not found.
	 */
	synchronized public PersistItem get(String key) {
		LOGGER.fine("Get operation executing for key " + key);

		Value v = map.get(key);
		if (v != null) {
			LOGGER.fine("Get operation found value " + v + " for key " + key + " in cache.");

			updateCache(key);
			return new PersistItem(key, v.data);
		}

		return null;
	}

	/**
	 * Puts the given key-value item.
	 * 
	 * @param item the key, value pair.
	 * @return PersistType.UPDATE if an item with the same key already exists,
	 *         PersistType.INSERT otherwise.
	 */
	synchronized public PersistType put(PersistItem item){
		Value v = map.get(item.key);
		if (v != null) {
			LOGGER.fine(
					"Put operation found value " + item.value + " for key " + item.key + " in cache and updating it.");

			updateCache(item.key);
			v.data = item.value;
			map.put(item.key, v);
			return PersistType.UPDATE;

		} else {
			LOGGER.fine("Put operation could not found value " + item.value + " for key " + item.key
					+ " in cache and inserting it into cache.");

			insertToCache(item);
			return PersistType.INSERT;
		}

	}

	/**
	 * Deletes the item with the given key.
	 * 
	 * @param key the key of the item to be deleted.
	 * @return PersistType.DELETE after delete operation is completed.
	 */
	synchronized public PersistType delete(String key) {
		Value v = map.get(key);
		if (v != null) {
			LOGGER.fine("Delete operation found value " + v + " for key " + key + " and removing it.");

			map.remove(key);
			deleteInternal(key);
		}

		return PersistType.DELETE;
	}

	protected void deleteInternal(String key) {

	}

	/**
	 * Updates the value for the given key according to the displacement strategy of
	 * this {@code CacheManager}.
	 * 
	 * @param key the key whose value should be updated.
	 */
	protected abstract void updateCache(String key);

	/**
	 * Inserts the given key-value item according to displacement strategy of this
	 * {@code CacheManager} into the cache.
	 * 
	 * @param item the item that should be inserted into this cache.
	 */
	protected void insertToCache(PersistItem item) {
		LOGGER.fine("The value " + item.value + " for key " + item.key
				+ " is inserting into the cache, according to the cache displacement strategy.");

		if (map.size() >= cacheSize) {
			removeFromCache();
		}

	}

	/**
	 * Removes an item from the cache according to the cache displacement strategy
	 * of this {@code CacheManager}.
	 */
	protected abstract void removeFromCache();

	/**
	 * Represents the value used in key and value pairs.
	 */
	protected static class Value {
		String data;
		long compareValue;

		/**
		 * Constructs a {@code Value} with the given data and value used for comparison.
		 * 
		 * @param data         the data of this {@code Value}.
		 * @param compareValue the value of this {@code Value}, which is used for
		 *                     comparison.
		 */
		public Value(String data, long compareValue) {
			this.data = data;
			this.compareValue = compareValue;
		}
	}
}
