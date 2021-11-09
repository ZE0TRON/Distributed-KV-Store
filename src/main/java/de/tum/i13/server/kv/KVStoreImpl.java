package de.tum.i13.server.kv;

import java.util.logging.Logger;

import de.tum.i13.server.kv.KVMessage.StatusType;
import de.tum.i13.server.storageManagment.CacheManager;

public class KVStoreImpl implements KVStore {
	
	 private static final Logger LOGGER = Logger.getLogger(KVStoreImpl.class.getName());

	@Override
	public KVMessage put(String key, String value) throws Exception {
		if (value != null) {
			try {
				CacheManager.getInstance().put(key, value);
				return new KVMessageImpl(key, value, StatusType.PUT_SUCCESS);
			} catch (Exception e) {
				LOGGER.throwing("KVStoreImpl", "put", e); // FIXME think about it again
				return new KVMessageImpl(key, value, StatusType.PUT_ERROR);
			}
		} else {
			try {
				CacheManager.getInstance().delete(key);
				return new KVMessageImpl(key, value, StatusType.DELETE_SUCCESS);
			} catch (Exception e) {
				LOGGER.throwing("KVStoreImpl", "put", e); // FIXME think about it again
				return new KVMessageImpl(key, value, StatusType.DELETE_ERROR);
			}
		}
	}

	@Override
	public KVMessage get(String key) throws Exception {
		try {
			CacheManager.getInstance().get(key);
			return new KVMessageImpl(key, null, StatusType.GET_SUCCESS);
		} catch (Exception e) {
			LOGGER.throwing("KVStoreImpl", "put", e); // FIXME think about it again
			return new KVMessageImpl(key, null, StatusType.GET_ERROR);
		}
	}

}
