package de.tum.i13.server.kv;

import java.util.ArrayList;
import java.util.logging.Logger;

import de.tum.i13.client.KeyRange;
import de.tum.i13.server.Main;
import de.tum.i13.server.kv.KVClientMessage.StatusType;
import de.tum.i13.server.storageManagment.CacheManager;
import de.tum.i13.server.storageManagment.PersistType;
import de.tum.i13.shared.ConnectionManager.EcsConnectionThread;

public class KVStoreImpl implements KVStore {

	private static final Logger LOGGER = Logger.getLogger(KVStoreImpl.class.getName());


	private static String metaDataString;
	private static ArrayList<KeyRange> metaData;
	private final Persist kvPersist;
	private final CacheManager cache;

	private static KeyRange keyRange;

	public KVStoreImpl() {
		kvPersist = Persist.getInstance();
		cache = CacheManager.getInstance();
	}

	@Override
	public KVClientMessage put(String key, String value) throws Exception {
		LOGGER.info("KVStoreImpl.put called with key: " + key + ", and value: " + value);
		try {
			if (value != null) {
				PersistItem item = new PersistItem(key, value);
				PersistType persistType = kvPersist.put(item);
				if (persistType == PersistType.INSERT) {
					cache.put(item);
					LOGGER.info("Put (insert) operation successfully executed on tuple: (" + key + ", " + value + ")");
					return new KVClientMessageImpl(key, value, StatusType.PUT_SUCCESS);
				} else {
					cache.put(item);
					LOGGER.info("Put (update) operation successfully executed on tuple: (" + key + ", " + value + ")");
					return new KVClientMessageImpl(key, value, StatusType.PUT_UPDATE);
				}

			} else {
				try {
					kvPersist.delete(key);
					LOGGER.fine("Delete operation successfully executed on key: " + key);
					return new KVClientMessageImpl(key, null, StatusType.DELETE_SUCCESS);
				} catch(Exception e) {
					LOGGER.fine("Delete operation not successfully executed on key: " + key + " because of key could not found");
					return new KVClientMessageImpl(key, null, StatusType.DELETE_ERROR);
				}
				finally {
					cache.delete(key);
				}
			}
		} catch (Exception e) {
			LOGGER.throwing("KVStoreImpl", "put on tuple: (" + key + ", "  + value + ")", e);
			return new KVClientMessageImpl(key, value, StatusType.PUT_ERROR);
		}
	}

	@Override
	public KVClientMessage get(String key) throws Exception {
		LOGGER.info("KVStoreImpl.get called with key: " + key);
		try {
			PersistItem item = cache.get(key);
			if (item != null) {
				LOGGER.fine("Cache hit value :" + item.value + " for key: " + item.key );
				return new KVClientMessageImpl(item.key, item.value, StatusType.GET_SUCCESS);
			} else {
				LOGGER.fine("Cache miss for key " + key );
				item = kvPersist.get(key);
				if (item != null) {
					LOGGER.fine("Disk hit value " + item.value + " for key " + item.key);
					cache.put(item);
					return new KVClientMessageImpl(item.key, item.value, StatusType.GET_SUCCESS);
				}
				else {
					LOGGER.fine("Get operation could not found value for key: " + key);
					return new KVClientMessageImpl(key, "", StatusType.GET_ERROR);
				}
			}
		} catch (Exception e) {
			LOGGER.throwing("KVStoreImpl", "get for key: " + key, e); // FIXME think about it again
			return new KVClientMessageImpl(key, null, StatusType.GET_ERROR);
		}
	}

	@Override
	public ArrayList<PersistItem> getAll() {
		LOGGER.info("KVStoreImpl.getAll called.");
		return kvPersist.deserializeItem().parts;
	}

	public KVClientMessage commandNotFound(String command) {
		LOGGER.info("KVStoreImpl.commandNotFound called.");
		// TODO Implement return description
		return new KVClientMessageImpl(null, null, StatusType.ERROR);
	}

	public KeyRange getKeyRange() {
		LOGGER.info("KVStoreImpl.getKeyRange called with keyRange: " + keyRange.toString());
		return keyRange;
	}

	public static String getMetaDataString() {
		LOGGER.info("KVStoreImpl.getMetaDataString called with metaDataString: " + metaDataString);
		return metaDataString;
	}

	public void updateKeyRange(ArrayList<KeyRange> metaData, String metaDataString){
		LOGGER.info("KVStoreImpl.updateKeyRange called with metaData: " + metaData.toString() + ",and metaDataString: " + metaDataString);

		KVStoreImpl.metaData = metaData;
		KVStoreImpl.metaDataString = metaDataString;

		String start = null, end = null;
		String kvServerAddr = Main.serverIp;
		int port = Main.port;
		for (KeyRange keyrange : metaData){
			if (keyrange.host.equals(kvServerAddr) && keyrange.port == port){
				start = keyrange.from;
				end = keyrange.to;
				break;
			}
		}
		keyRange = new KeyRange(start, end, kvServerAddr, port);
	}



}
