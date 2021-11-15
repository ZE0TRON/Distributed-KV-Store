package de.tum.i13.server.storageManagment;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Logger;

public class CacheManager implements DataManager {

	private static final Logger LOGGER = Logger.getLogger(CacheManager.class.getName());

	private static CacheManager instance;

	private int cacheSize;

	private CacheDisplacementStrategy cds;

	private HashMap<String, Value> map;

	// it is for implementation of FIFO
	private LinkedList<String> keyList;

	private CacheManager() {
		// private constructor
		map = new HashMap<>();
		keyList = new LinkedList<>();
	}

	synchronized public static void init(int cacheSize, CacheDisplacementStrategy cds) {
		if (instance == null) {
			LOGGER.fine("CacheManager is initialized with cache size:" + cacheSize
					+ " and cache displacement strategy: " + cds);

			instance = new CacheManager();
			instance.cacheSize = cacheSize;
			instance.cds = cds;

		}
	}

	public static CacheManager getInstance() {
		if (instance == null) {
			throw new RuntimeException("Initialize CacheManager first");
		}

		return instance;
	}

	/**
	 * Get from the file
	 * 
	 * @param key
	 * @return value for the given key, null if key not found
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

	private void removeFromCache() {
		LOGGER.fine("Removing an item from the cache, according to cache displacement strategy.");

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

	private void removeForFIFO() {
		String key = keyList.removeFirst();
		map.remove(key);
	}

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
	 * Put to the file
	 * 
	 * @param key
	 * @param value
	 * @return true if key value pair is inserted, if value for key is updated,
	 *         return false
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
	 * Delete from the file
	 * 
	 * @param key
	 * @return
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

	private static class Value {
		String data;
		long compareValue;

		public Value(String data, long compareValue) {
			this.data = data;
			this.compareValue = compareValue;
		}
	}
}
