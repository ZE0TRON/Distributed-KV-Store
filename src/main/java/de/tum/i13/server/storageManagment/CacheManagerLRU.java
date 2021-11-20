package de.tum.i13.server.storageManagment;

import de.tum.i13.server.kv.KVItem;

import java.util.Map;
import java.util.logging.Logger;

public class CacheManagerLRU extends CacheManager {

	private static final Logger LOGGER = Logger.getLogger(CacheManagerLRU.class.getName());

	/**
	 * Initializes this {@code CacheManagerLRU} with the given cache size if it is
	 * not already initialized.
	 * 
	 * @param cacheSize the cache size of this {@code CacheManagerLRU}
	 */
	synchronized protected static void init(int cacheSize) {
		if (!isInitialized()) {
			LOGGER.fine("CacheManagerLRU is initialized with cache size:" + cacheSize);

			instance = new CacheManagerLRU();
			instance.cacheSize = cacheSize;
		}
	}

	/**
	 * Updates the pair with the given key according to the LRU strategy.
	 * 
	 * @param key the key of the pair to be updated.
	 */
	@Override
	protected void updateCache(String key) {
		Value value = map.get(key);

		LOGGER.fine("The value :" + value + " for key: " + key + " is updating for LRU");

		value.compareValue = System.currentTimeMillis();

	}

	/**
	 * Inserts the pair of key and value according to the LRU strategy.
	 * 
	 * @param item the key, value pair.
	 */
	@Override
	protected void insertToCache(KVItem item) {
		super.insertToCache(item);

		Value v = new Value(item.value, System.currentTimeMillis());
		map.put(item.key, v);

	}

	/**
	 * Removes the appropriate item from the cache according to the LRU strategy.
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
