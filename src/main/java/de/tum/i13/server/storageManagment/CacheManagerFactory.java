package de.tum.i13.server.storageManagment;

public class CacheManagerFactory {

	/**
	 * Creates a subclass of {@code CacheManager} according to the given
	 * {@code CacheDisplacementStrategy} and cache size.
	 * 
	 * @param cacheSize the cache size of the cache manager to be created.
	 * @param cds       the cache displacement strategy of the cache manager to be
	 *                  created.
	 * @return {@code CacheManagerFIFO}, {@code CacheManagerLRU},
	 *         {@code CacheManagerLFU} if the given cache displacement strategy is
	 *         FIFO, LRU, LFU respectively with the given cache size.
	 */
	public static CacheManager create(int cacheSize, CacheDisplacementStrategy cds) {
		if (CacheManager.isInitialized())
			throw new RuntimeException("CacheManager is already initialized!");

		switch (cds) {
		case FIFO:
			CacheManagerFIFO.init(cacheSize);
			break;
		case LRU:
			CacheManagerLRU.init(cacheSize);
			break;
		case LFU:
			CacheManagerLFU.init(cacheSize);
			break;
		}

		return CacheManager.getInstance();
	}

}
