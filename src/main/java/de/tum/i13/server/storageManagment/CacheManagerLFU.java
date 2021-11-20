package de.tum.i13.server.storageManagment;

import java.util.Map;
import java.util.logging.Logger;

public class CacheManagerLFU extends CacheManager {

	private static final Logger LOGGER = Logger.getLogger(CacheManagerLFU.class.getName());

	/**
	 * Initializes this {@code CacheManagerLFU} with the given cache size if it is
	 * not already initialized.
	 * 
	 * @param cacheSize the cache size of this {@code CacheManagerLFU}
	 */
	synchronized protected static void init(int cacheSize) {
		if (!isInitialized()) {
			LOGGER.fine("CacheManagerLFU is initialized with cache size:" + cacheSize);

			instance = new CacheManagerLFU();
			instance.cacheSize = cacheSize;
		}
	}

	/**
	 * Updates the pair with the given key according to the LFU strategy.
	 * 
	 * @param key the key of the pair to be updated.
	 */
	@Override
	protected void updateCache(String key) {
		Value value = map.get(key);

		LOGGER.fine("The value :" + value + " for key: " + key + " is updating for LFU");

		value.compareValue++;

	}

	/**
	 * Inserts the pair of key and value according to the LFU strategy into the
	 * cache.
	 * 
	 * @param key   the key of this pair.
	 * @param value the value of this pair.
	 */
	@Override
	protected void insertToCache(String key, String value) {
		super.insertToCache(key, value);

		Value v = new Value(value, 1);
		map.put(key, v);

	}

	/**
	 * Removes the appropriate item from the cache according to the LFU strategy.
	 */
	@Override
	protected void removeFromCache() {
		LOGGER.fine("Removing an item from the cache");

		String minKey = "";
		long min = Long.MAX_VALUE;

		for (Map.Entry<String, Value> entry : map.entrySet()) {
			if (entry.getValue().compareValue < min) {
				minKey = entry.getKey();
				min = entry.getValue().compareValue;
			}
		}

		map.remove(minKey);
	}

}