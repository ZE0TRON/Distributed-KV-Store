package de.tum.i13.server.storageManagment;

import de.tum.i13.server.kv.PersistItem;

import java.util.LinkedList;
import java.util.logging.Logger;

public class CacheManagerFIFO extends CacheManager {

	private static final Logger LOGGER = Logger.getLogger(CacheManagerFIFO.class.getName());

	// It is for the implementation of FIFO strategy.
	protected LinkedList<String> keyList;

	/**
	 * Constructs a {@code CacheManagerFIFO} and initializes its list of keys.
	 */
	private CacheManagerFIFO() {
		keyList = new LinkedList<>();
	}

	/**
	 * Initializes this {@code CacheManagerFIFO} with the given cache size if it is
	 * not already initialized.
	 * 
	 * @param cacheSize the cache size of this {@code CacheManagerFIFO}
	 */
	synchronized protected static void init(int cacheSize) {
		if (!isInitialized()) {
			LOGGER.fine("CacheManagerFIFO is initialized with cache size:" + cacheSize);

			instance = new CacheManagerFIFO();
			instance.cacheSize = cacheSize;
		}
	}

	/**
	 * Updates the value for the given key according to the FIFO strategy.
	 * 
	 * @param key the key whose value should be updated.
	 */
	@Override
	protected void updateCache(String key) {
		// Do nothing
	}

	/**
	 * Inserts the given key-value item according to the FIFO strategy into the
	 * cache.
	 * 
	 * @param item the item that should be inserted into this cache.
	 */
	@Override
	protected void insertToCache(PersistItem item) {
		super.insertToCache(item);

		Value v = new Value(item.value, 0);
		keyList.add(item.key);
		map.put(item.key, v);

	}

	/**
	 * Removes an item from the cache according to the FIFO strategy.
	 */
	@Override
	protected void removeFromCache() {
		LOGGER.fine("Removing an item from the cache");

		String key = keyList.removeFirst();
		map.remove(key);
	}

	/**
	 * Deletes the given key according to the FIFO strategy.
	 * 
	 * @param key the key to be deleted.
	 */
	@Override
	protected void deleteInternal(String key) {
		keyList.remove(key);
	}

}
