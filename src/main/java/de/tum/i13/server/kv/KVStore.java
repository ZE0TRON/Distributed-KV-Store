package de.tum.i13.server.kv;

import de.tum.i13.client.KeyRange;
import de.tum.i13.server.exception.NoSuchSubscriptionException;
import de.tum.i13.shared.Server;

import java.util.ArrayList;
import java.util.HashMap;

public interface KVStore {

    /**
     * Inserts a key-value pair into the KVServer.
     *
     * @param key   the key that identifies the given value.
     * @param value the value that is indexed by the given key.
     * @return a message that confirms the insertion of the tuple or an error.
     * @throws Exception if put command cannot be executed (e.g. not connected to any
     *                   KV server).
     */
    KVClientMessage put(String key, String value, String caller) throws Exception;

    /**
     * Retrieves the value for a given key from the KVServer.
     *
     * @param key the key that identifies the value.
     * @return the value, which is indexed by the given key.
     * @throws Exception if put command cannot be executed (e.g. not connected to any
     *                   KV server).
     */
    KVClientMessage get(String key) throws Exception;

    void updateKeyRange(ArrayList<KeyRange> metadata, String metadataString, String metadataType);

    ArrayList<PersistItem> getAll();

    KVClientMessage commandNotFound(String command);

    HashMap<String, ArrayList<Server>> getSubscriptions();

    void addSubscription(String key, String addr) throws Exception;

    void deleteSubscription(String key, String addr) throws NoSuchSubscriptionException;
}
