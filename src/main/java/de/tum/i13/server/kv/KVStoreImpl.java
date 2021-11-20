package de.tum.i13.server.kv;

import java.util.logging.Logger;

import de.tum.i13.server.kv.KVMessage.StatusType;
import de.tum.i13.server.storageManagment.CacheManager;

public class KVStoreImpl implements KVStore {

	private static final Logger LOGGER = Logger.getLogger(KVStoreImpl.class.getName());

	@Override
	public KVMessage put(String key, String value) throws Exception {
		try {
			if (value != null) {
				if (CacheManager.getInstance().put(key, value)) {
					LOGGER.fine("Put (insert) operation successfully executed on tuple: (" + key + ", " + value + ")");
					return new KVMessageImpl(key, value, StatusType.PUT_SUCCESS);
				} else {
					LOGGER.fine("Put (update) operation successfully executed on tuple: (" + key + ", " + value + ")");
					return new KVMessageImpl(key, value, StatusType.PUT_UPDATE);
				}
			} else {
				if (CacheManager.getInstance().delete(key)) {
					LOGGER.fine("Delete operation successfully executed on key: " + key);
					return new KVMessageImpl(key, value, StatusType.DELETE_SUCCESS);
				} else {
					LOGGER.fine("Delete operation not successfully executed on key: " + key + " becauseof key could not found");
					return new KVMessageImpl(key, value, StatusType.DELETE_ERROR);
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
			String value = CacheManager.getInstance().get(key);
			if (value != null) {
				LOGGER.fine("Get operation found value :" + value + " for key: " + key );
				return new KVMessageImpl(key, value, StatusType.GET_SUCCESS);
			} else {
				LOGGER.fine("Get operation could not found value for key: " + key );
				return new KVMessageImpl(key, null, StatusType.GET_ERROR);
			}
		} catch (Exception e) {
			LOGGER.throwing("KVStoreImpl", "get for key: " + key, e); // FIXME think about it again
			return new KVMessageImpl(key, null, StatusType.GET_ERROR);
		}
	}

}
