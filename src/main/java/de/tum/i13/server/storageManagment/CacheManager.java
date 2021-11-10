package de.tum.i13.server.storageManagment;

public class CacheManager implements DataManager {

	private static CacheManager instance;

	private int cacheSize;
	private String cacheDisplacementStrategy;

	private CacheManager() {
		// private constructor
	}

	synchronized public static void init(int cacheSize, String cacheDisplacementStrategy) {
		if (instance == null) {
			instance = new CacheManager();
			// TODO check path and create file if necessary
			instance.cacheSize = cacheSize;
			instance.cacheDisplacementStrategy = cacheDisplacementStrategy;
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
	 * @return
	 */
	synchronized public String get(String key) {
		// FIXME implement cache functionality
		return DiskManager.getInstance().get(key);
	}

	/**
	 * Put to the file
	 * 
	 * @param key
	 * @param value
	 */
	synchronized public void put(String key, String value) {
		// FIXME implement cache functionality
		DiskManager.getInstance().put(key, value);
	}

	/**
	 * Delete from the file
	 * 
	 * @param key
	 * @return
	 */
	synchronized public boolean delete(String key) {
		// FIXME implement cache functionality
		DiskManager.getInstance().delete(key);
		return true;
	}

}
