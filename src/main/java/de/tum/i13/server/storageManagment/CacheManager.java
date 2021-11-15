package de.tum.i13.server.storageManagment;

import java.util.HashMap;

import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Logger;

public class CacheManager implements DataManager {

	private static final Logger LOGGER = Logger.getLogger(CacheManager.class.getName());

	private static CacheManager instance;

	protected int cacheSize;

	protected CacheDisplacementStrategy cds;

	protected HashMap<String, Value> map;

	// It is for the implementation of FIFO strategy.
	protected LinkedList<String> keyList;

	/**
	 * Constructs a {@code CacheManager} and initializes its map and list of keys.
	 */
	private CacheManager() {
		// private constructor
		map = new HashMap<>();
		keyList = new LinkedList<>();
	}

	/**
	 * Initializes this {@code CacheManager} with the given cache size and cache
	 * displacement strategy if it is not already initialized.
	 * 
	 * @param cacheSize the cache size of this {@code CacheManager}
	 * @param cds       the cache displacement strategy used by this
	 *                  {@code CacheManager}
	 */
	synchronized public static void init(int cacheSize, CacheDisplacementStrategy cds) {
		if (instance == null) {
			LOGGER.fine("CacheManager is initialized with cache size:" + cacheSize
					+ " and cache displacement strategy: " + cds);

			instance = new CacheManager();
			instance.cacheSize = cacheSize;
			instance.cds = cds;

		}
	}

	/**
	 * Gets the instance of this {@code CacheManager}.
	 * 
	 * @return this {@code CacheManager} if it is already initialized before.
	 * @throws RuntimeException if this {@code CacheManager} is not already
	 *                          initialized.
	 */
	public static CacheManager getInstance() {
		if (instance == null) {
			throw new RuntimeException("Initialize CacheManager first!");
		}

		return instance;
	}

	/**
	 * Gets the value for the given key.
	 * 
	 * @param key the key whose value is wanted.
	 * @return value for the given key, null if key is not found.
	 */
	synchronized public String get(String key) {
		LOGGER.fine("Get operation executiong for key: " + key);

		Value v = map.get(key);
		if (v != null) {
			LOGGER.fine("Get operation found value :" + v + " for key: " + key + "in cache.");

			updateCache(key);
			return v.data;
		}

		String value = DiskManager.getInstance().get(key);
		if (value != null) {
			LOGGER.fine(
					"Get operation found value :" + value + " for key: " + key + "in disk and inserted into cache.");

			insertToCache(key, value);
		}

		return value;
	}

	/**
	 * Updates the value for the given key according to displacement strategy of
	 * this {@code CacheManager}.
	 * 
	 * @param key the key whose value should be updated.
	 */
	private void updateCache(String key) {
		Value value = map.get(key);

		LOGGER.fine(
				"The value :" + value + " for key: " + key + " is updating according to cache displacement strategy.");

		switch (cds) {
		case LRU:
			value.compareValue = System.currentTimeMillis();
			break;
		case LFU:
			value.compareValue++;
			break;
		case FIFO:
			// Do nothing
			break;
		}
	}

	/**
	 * Inserts the pair of key and value according to displacement strategy of this
	 * {@code CacheManager} into the cache.
	 * 
	 * @param key   the key of this pair.
	 * @param value the value of this pair.
	 */
	private void insertToCache(String key, String value) {
		LOGGER.fine("The value :" + value + " for key: " + key
				+ " is inserting to the cache, according to cache displacement strategy.");

		if (map.size() >= cacheSize) {
			removeFromCache();
		}

		Value v = null;

		switch (cds) {
		case FIFO:
			v = new Value(value, 0);
			keyList.add(key);
			break;
		case LRU:
			v = new Value(value, System.currentTimeMillis());
			break;
		case LFU:
			v = new Value(value, 1);
			break;
		}

		map.put(key, v);

	}

	/**
	 * Removes an item from the cache according to the cache displacement strategy
	 * of this {@code CacheManager}.
	 */
	private void removeFromCache() {
		LOGGER.fine("Removing an item from the cache, according to the cache displacement strategy.");

		switch (cds) {
		case FIFO:
			removeForFIFO();
			break;
		case LRU:
		case LFU:
			removeForLRUOrLFU();
			break;
		}
	}

	/**
	 * Removes an item from the cache according to the FIFO strategy.
	 */
	private void removeForFIFO() {
		String key = keyList.removeFirst();
		map.remove(key);
	}

	/**
	 * Removes an item from the cache according to the LRU or LFU strategy.
	 */
	private void removeForLRUOrLFU() {
		String minKey = "";
		long min = Long.MAX_VALUE;

		for (Map.Entry<String, Value> entry : map.entrySet()) {
			if (entry.getValue().compareValue < min) {
				minKey = entry.getValue().data;
				min = entry.getValue().compareValue;
			}
		}

		map.remove(minKey);

	}

	/**
	 * Puts the given key and value pair.
	 * 
	 * @param key   the of this pair.
	 * @param value the value of this pair.
	 * @return true if key and value pair is inserted, false if value for key is
	 *         updated,
	 */
	synchronized public boolean put(String key, String value) {
		LOGGER.fine("Put operation executing on value :" + value + " for key: " + key);

		boolean isInsert = DiskManager.getInstance().put(key, value);

		Value v = map.get(key);
		if (v != null) {
			LOGGER.fine("Put operation found value :" + value + " for key: " + key + " in cache and updating it.");

			updateCache(key);
			v.data = value;
			map.put(key, v);

		} else {
			LOGGER.fine("Put operation could not found value :" + value + " for key: " + key
					+ " in cache and inserting it into cache.");

			insertToCache(key, value);
		}

		return isInsert;
	}

	/**
	 * Deletes the item with the given key.
	 * 
	 * @param key the key of the item to be deleted.
	 * @return true if the item is deleted, false if it could not be deleted or not
	 *         found.
	 */
	synchronized public boolean delete(String key) {
		LOGGER.fine("Delete operation executing on key: " + key);

		boolean isRemoved = DiskManager.getInstance().delete(key);

		if (isRemoved) {
			Value v = map.get(key);
			if (v != null) {
				LOGGER.fine("Delete operation found value: " + v + " for key: " + key + " and removing it.");

				map.remove(key);
				if (cds == CacheDisplacementStrategy.FIFO) {
					keyList.remove(key); // O(n)
				}

			}
		}

		return isRemoved;
	}

	/**
	 * Represents the value used in key and value pairs.
	 */
	private static class Value {
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
