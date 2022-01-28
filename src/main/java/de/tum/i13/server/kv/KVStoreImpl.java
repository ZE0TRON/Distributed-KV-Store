package de.tum.i13.server.kv;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import de.tum.i13.client.KeyRange;
import de.tum.i13.server.Main;
import de.tum.i13.server.exception.NoSuchSubscriptionException;
import de.tum.i13.server.kv.KVClientMessage.StatusType;
import de.tum.i13.server.storageManagment.CacheManager;
import de.tum.i13.server.storageManagment.PersistType;
import de.tum.i13.shared.ConnectionManager.ConnectionManager;
import de.tum.i13.shared.ConnectionManager.NotificationThread;
import de.tum.i13.shared.Server;
import de.tum.i13.shared.Util;
import de.tum.i13.ecs.keyring.ConsistentHashingService;

public class KVStoreImpl implements KVStore {

	private static final Logger LOGGER = Logger.getLogger(KVStoreImpl.class.getName());

	private static String coordinatorMetadataString;
	private static ArrayList<KeyRange> coordinatorMetadata;
	private static String wholeMetadataString;



	private static ArrayList<KeyRange> wholeMetadata;
	private final Persist kvPersist;
	private final CacheManager cache;

	private static KeyRange coordinatorKeyRange;
	private static KeyRange wholeKeyRange;

	private static String firstSuccessor;
	private static String secondSuccessor;
	public static Socket replica1Connection = null;
	public static Socket replica2Connection = null;
	public static KVStoreImpl instance = null;

	private HashMap<String,ArrayList<Server>> subscriptions;

	public HashMap<String, ArrayList<Server>> getSubscriptions() {
		return subscriptions;
	}

	private KVStoreImpl() {
		kvPersist = Persist.getInstance();
		cache = CacheManager.getInstance();
		subscriptions = new HashMap<>();
	}

	public static KVStoreImpl getInstance() {
		if (instance == null){
			instance = new KVStoreImpl();
		}
		return instance;
	}

	@Override
	public synchronized KVClientMessage put(String key, String value, String caller) throws Exception {
		if (Util.isKeyInRange(coordinatorKeyRange.from, coordinatorKeyRange.to, ConsistentHashingService.findHash(key)) || caller.equals("self")){
			LOGGER.info("KVStoreImpl.put called with key: " + key + ", and value: " + value);
			try {
				if (value != null) {
					PersistItem item = new PersistItem(key, value);
					PersistType persistType = kvPersist.put(item);
					if (persistType == PersistType.INSERT) {
						cache.put(item);
						LOGGER.info("Put (insert) operation successfully executed on tuple: (" + key + ", " + value + ")");
						notifyAllClientsSubscribedToKey(key, value , StatusType.NOTIFICATION_UPDATE);
						return new KVClientMessageImpl(key, null, StatusType.PUT_SUCCESS);
					} else {
						cache.put(item);
						LOGGER.info("Put (update) operation successfully executed on tuple: (" + key + ", " + value + ")");
						notifyAllClientsSubscribedToKey(key, value , StatusType.NOTIFICATION_UPDATE);
						return new KVClientMessageImpl(key, null, StatusType.PUT_UPDATE);
					}

				} else {
					try {
						kvPersist.delete(key);
						LOGGER.fine("Delete operation successfully executed on key: " + key);
						notifyAllClientsSubscribedToKey(key, null , StatusType.NOTIFICATION_DELETE);
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
				LOGGER.warning("Exception "+ e.getMessage() +" KVStoreImpl while putting tuple" + key + ", "  + value );
				return new KVClientMessageImpl(key, value, StatusType.PUT_ERROR);
			}
		} else {
			LOGGER.info("KVStoreImpl.put called with incorrect key. This server is not responsible for the key: " + key + ", and value: " + value);
			if (value != null){
				return new KVClientMessageImpl(key, value, StatusType.PUT_ERROR);
			}
			else {
				return new KVClientMessageImpl(key, value, StatusType.DELETE_ERROR);
			}
		}
	}

	@Override
	public KVClientMessage get(String key) throws Exception {
		if (Util.isKeyInRange(wholeKeyRange.from, wholeKeyRange.to, ConsistentHashingService.findHash(key))) {
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
		} else {
			LOGGER.info("KVStoreImpl.get called with incorrect key. This server is not responsible for the key: " + key);
			return new KVClientMessageImpl(key, null, StatusType.GET_ERROR);
		}
	}

	@Override
	public ArrayList<PersistItem> getAll() {
		LOGGER.info("KVStoreImpl.getAll called.");
		PersistItemCollection collection = kvPersist.deserializeItem();
		return collection == null ? new ArrayList<>() : collection.parts;
	}

	public KVClientMessage commandNotFound(String command) {
		LOGGER.info("KVStoreImpl.commandNotFound called.");
		// TODO Implement return description
		return new KVClientMessageImpl(null, null, StatusType.ERROR);
	}

	public static KeyRange getCoordinatorKeyRange() {
		LOGGER.info("KVStoreImpl.getKeyRange called with keyRange: " + KVStoreImpl.coordinatorKeyRange.toString());
		return KVStoreImpl.coordinatorKeyRange;
	}

	public static String getCoordinatorMetadataString() {
		LOGGER.info("KVStoreImpl.getCoordinatorMetadataString called with CoordinatorMetadataString: " + coordinatorMetadataString);
		return coordinatorMetadataString;
	}

	public static String getWholeMetadataString() {
		LOGGER.info("KVStoreImpl.getWholeMetadataString called with WholeMetadataString: " + wholeMetadataString);
		return wholeMetadataString;
	}


	public void updateKeyRange(ArrayList<KeyRange> metadata, String metadataString, String metadataType){
		LOGGER.info("KVStoreImpl.updateKeyRange called with metadata: " + metadata.toString() + ", metadataString: "
				+ metadataString + " and metadataType: " + metadataType);

		String start = null, end = null;
		String kvServerAddr = Main.serverIp;
		LOGGER.info("updateKeyRange Server IP: " + Main.serverIp);
		int port = Main.port;
		for (KeyRange keyrange : metadata){
			if (keyrange.host.equals(kvServerAddr) && keyrange.port == port){
				start = keyrange.from;
				end = keyrange.to;
				break;
			}
		}
		if (metadataType.equals("coordinator")){
			KVStoreImpl.coordinatorMetadata = metadata;
			KVStoreImpl.coordinatorMetadataString = metadataString;
			KVStoreImpl.coordinatorKeyRange = new KeyRange(start, end, kvServerAddr, port);
		} else if (metadataType.equals("replica")){
			KVStoreImpl.wholeMetadata = metadata;
			KVStoreImpl.wholeMetadataString = metadataString;
			KVStoreImpl.wholeKeyRange = new KeyRange(start, end, kvServerAddr, port);
		}
	}

	public void dropKeys(){
		ArrayList<PersistItem> allItems = getAll();
		PersistItemCollection itemsToSave = new PersistItemCollection();
		for (PersistItem item : allItems){
			if (Util.isKeyInRange(wholeKeyRange.from, wholeKeyRange.to, item.key)){
				itemsToSave.parts.add(item);
			}
		}
		kvPersist.serializeAndPersistItem(itemsToSave);
	}

	public static String getFirstSuccessor() {
		return firstSuccessor;
	}

	public static void setFirstSuccessor(String firstSuccessor) {
		KVStoreImpl.firstSuccessor = firstSuccessor;
	}

	public static String getSecondSuccessor() {
		return secondSuccessor;
	}

	public static void setSecondSuccessor(String secondSuccessor) {
		KVStoreImpl.secondSuccessor = secondSuccessor;
	}

	public static void setReplicaConnections() {
		String addr1 = firstSuccessor.split(":")[0];
		String port1 = firstSuccessor.split(":")[1];
		String addr2 = secondSuccessor.split(":")[0];
		String port2 = secondSuccessor.split(":")[1];
		try {
			if (replica1Connection != null) {
				replica1Connection.close();
				replica1Connection = null;
			}
			if (replica2Connection != null) {
				replica2Connection.close();
				replica2Connection = null;
			}
			replica1Connection = new Socket(addr1, Integer.parseInt(port1));
			replica2Connection = new Socket(addr2, Integer.parseInt(port2));
		} catch (Exception e) {
			LOGGER.warning("Exception while setReplicaConnections " + e.getMessage() );
		}


	}

	public static KeyRange getWholeKeyRange() {
		LOGGER.info("KVStoreImpl.getWholeKeyRange called with keyRange: " + KVStoreImpl.wholeKeyRange.toString());
		return wholeKeyRange;
	}

	public void addSubscription(String key, String addr) throws Exception {
		Server client = Server.fromHashableString(addr);
		// Try to connect to the client if successful add subscription
		try {
			Socket clientSocket = new Socket(client.getAddress(), Integer.parseInt(client.getPort()));
			ConnectionManager connection = new ConnectionManager(clientSocket);
			connection.disconnect();
		} catch (IOException e) {
			LOGGER.warning("Exception while sending notification to client " + e.getMessage());
			throw new Exception("Subscription failed with IOExpcetion" + e.getMessage());
		}
		if (this.subscriptions.containsKey(key)) {
			ArrayList<Server> clients = this.subscriptions.get(key);
			if (!clients.contains(client)) {
				clients.add(client);
			}
		} else {
			ArrayList<Server> clients = new ArrayList<>();
			clients.add(client);
			this.subscriptions.put(key, clients);
		}
	}

	public void deleteSubscription(String key, String addr) throws NoSuchSubscriptionException {
		Server client = Server.fromHashableString(addr);
		if (this.subscriptions.containsKey(key)) {
			ArrayList<Server> clients = this.subscriptions.get(key);
			if(clients.contains(client)) {
				clients.remove(client);
			} else {
				throw new NoSuchSubscriptionException();
			}
		} else {
			throw new NoSuchSubscriptionException();
		}
	}

	private void notifyAllClientsSubscribedToKey(String key, String newValue, StatusType notificationType) {
		if (!this.subscriptions.containsKey(key)) {
			return;
		}
		ArrayList<Server> clients = this.subscriptions.get(key);
		if( clients != null && clients.size() > 0) {
			for (Server client : clients) {
				notify(key, newValue, notificationType, client);
			}
		}
	}

	private void notify(String key, String newValue, StatusType notificationType,  Server server) {
		NotificationThread nt = new NotificationThread(key, newValue, notificationType, server);
		nt.run();
	}

}
