package de.tum.i13.server.storageManagment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class DiskManager implements DataManager {

	private static DiskManager instance;

	private Properties properties;

	private DiskManager() {
		// private constructor
	}

	synchronized static public void init(Path dataDir) throws IOException {
		if (instance == null) {
			instance = new DiskManager();
			// TODO check path and create file if necessary
			Properties properties = new Properties();
			properties.load(Files.newInputStream(dataDir));
		}

	}

	public static DiskManager getInstance() {
		if (instance == null) {
			throw new RuntimeException("Initialize DiskManager first");
		}

		return instance;
	}

	/**
	 * Get from the file
	 * 
	 * @param key
	 * @return
	 */
	@Override
	synchronized public String get(String key) {
		return properties.getProperty(key);
	}

	/**
	 * Put to the file
	 * 
	 * @param key
	 * @param value
	 * @return true if key value pair is inserted, if value for key is updated, return false
	 */
	@Override
	synchronized public boolean put(String key, String value) {
		properties.setProperty(key, value);
		return false;
	}

	/**
	 * Delete from the file
	 * 
	 * @param key
	 * @return
	 */
	@Override
	synchronized public boolean delete(String key) {
		String value = (String) properties.remove(key);
		return value == null ? false : true;
	}
}
