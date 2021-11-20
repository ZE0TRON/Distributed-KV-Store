package de.tum.i13.server.storageManagment;

public class CacheManagerFactory {
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
