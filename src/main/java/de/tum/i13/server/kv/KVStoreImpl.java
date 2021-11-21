package de.tum.i13.server.kv;

import java.util.logging.Logger;

import de.tum.i13.server.kv.KVMessage.StatusType;
import de.tum.i13.server.storageManagment.CacheManager;
import de.tum.i13.server.storageManagment.DataManager;
import de.tum.i13.server.storageManagment.PersistType;

public class KVStoreImpl implements KVStore {

	private static final Logger LOGGER = Logger.getLogger(KVStoreImpl.class.getName());
	private final DataManager kvPersist;
	private final DataManager cache;
	public KVStoreImpl() {
		kvPersist = KVPersist.getInstance();
		cache = CacheManager.getInstance();
	}

	@Override
	public KVMessage put(String key, String value) throws Exception {
		try {
			if (value != null) {
				KVItem item = new KVItem(key, value);
				PersistType persistType = kvPersist.put(item);
				if (persistType == PersistType.INSERT) {
					cache.put(item);
					LOGGER.info("Put (insert) operation successfully executed on tuple: (" + key + ", " + value + ")");
					return new KVMessageImpl(key, value, StatusType.PUT_SUCCESS);
				} else {
					cache.put(item);
					LOGGER.info("Put (update) operation successfully executed on tuple: (" + key + ", " + value + ")");
					return new KVMessageImpl(key, value, StatusType.PUT_UPDATE);
				}

			} else {
				try {
					kvPersist.delete(key);
					LOGGER.fine("Delete operation successfully executed on key: " + key);
					return new KVMessageImpl(key, value, StatusType.DELETE_SUCCESS);
				} catch(Exception e) {
					LOGGER.fine("Delete operation not successfully executed on key: " + key + " because of key could not found");
					return new KVMessageImpl(key, value, StatusType.DELETE_ERROR);
				}
				finally {
					cache.delete(key);
				}
			}
		} catch (Exception e) {
			LOGGER.throwing("KVStoreImpl", "put on tuple: (" + key + ", "  + value + ")", e);
			return new KVMessageImpl(key, value, StatusType.PUT_ERROR);
		}
	}

	@Override
	public KVMessage get(String key) throws Exception {
		try {
			KVItem item = cache.get(key);
			if (item != null) {
				LOGGER.fine("Cache hit value :" + item.value + " for key: " + item.key );
				return new KVMessageImpl(item.key, item.value, StatusType.GET_SUCCESS);
			} else {
				LOGGER.fine("Cache miss for key " + key );
				item = kvPersist.get(key);
				if (item != null) {
					LOGGER.fine("Disk hit value " + item.value + " for key " + item.key);
					cache.put(item);
					return new KVMessageImpl(item.key, item.value, StatusType.GET_SUCCESS);
				}
				else {
					LOGGER.fine("Get operation could not found value for key: " + key);
					return new KVMessageImpl(key, "", StatusType.GET_ERROR);
				}
			}
		} catch (Exception e) {
			LOGGER.throwing("KVStoreImpl", "get for key: " + key, e); // FIXME think about it again
			return new KVMessageImpl(key, null, StatusType.GET_ERROR);
		}
	}

	public KVMessage commandNotFound(String command) {
		// TODO Implement return description
		return new KVMessageImpl(null, null, StatusType.ERROR);
	}
}
