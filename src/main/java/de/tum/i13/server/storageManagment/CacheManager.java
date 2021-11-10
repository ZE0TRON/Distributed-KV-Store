package de.tum.i13.server.storageManagment;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class CacheManager implements DataManager {

	private static CacheManager instance;

	private int cacheSize;
	
	private CacheDisplacementStrategy cds;
	
	private HashMap<String, Value> map;
	
	// it is for implementation FIFO
	private LinkedList<String> keyList;

	private CacheManager() {
		// private constructor
		map = new HashMap<>();
		keyList = new LinkedList<>();
	}

	synchronized public static void init(int cacheSize, CacheDisplacementStrategy cds) {
		if (instance == null) {
			instance = new CacheManager();
			// TODO check path and create file if necessary
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
		// FIXME implement cache functionality
		Value v = map.get(key);
		if (v != null) {
			updateCache(key);
			return v.data;
		}
		
		String value =  DiskManager.getInstance().get(key);
		if (value != null) {
			insertToCache(key, value);
		}
		
		return value;
	}

	private void updateCache(String key) {
		Value value = map.get(key);
		switch (cds) {
			case LRU: value.compareValue = System.currentTimeMillis(); break;
			case LFU: value.compareValue++; break;
		}
	}

	private void insertToCache(String key, String value) {
		if (map.size() >= cacheSize) {
			removeFromCache();
		}
		
		Value v = null;
		
		switch (cds) {
			case FIFO: v = new Value(value, 0); keyList.add(key); break;
			case LRU: v = new Value(value, System.currentTimeMillis()); break;
			case LFU: v = new Value(value, 1); break;
		}
		
		map.put(key, v);
		
	}

	private void removeFromCache() {
		switch (cds) {
			case FIFO: removeForFIFO(); break;
			case LRU:
			case LFU: removeForLRUOrLFU(); break;
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
		boolean isInsert = DiskManager.getInstance().put(key, value);
		Value v = map.get(key);
		if (v != null) {
			updateCache(key);
			v.data = value;
			map.put(key, v);
		} else {
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
		boolean isRemoved = DiskManager.getInstance().delete(key);
		
		if (isRemoved) {
			Value v = map.get(key);
			if (v != null) {
				map.remove(key);
				if (cds == CacheDisplacementStrategy.FIFO) {
					keyList.remove(key);
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
