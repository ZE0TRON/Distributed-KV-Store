package de.tum.i13.ecs.cs;

import de.tum.i13.ecs.keyring.*;
import de.tum.i13.shared.Constants;

import java.security.NoSuchAlgorithmException;

public class ECS implements ConfigurationService {

    private static ECS instance;
    private KeyRingService keyRingService;
    private HashService hashService;
    private static final String HASHING_ALGORITHM = "md5";

    // TODO lock the correct functions like delete or insert in BST
    private ECS() {
        this.keyRingService = KeyRingService.getInstance();
        try {
            this.hashService = new ConsistentHashingService(HASHING_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            // TODO Log here
            e.printStackTrace();
        }
    }

    public static ECS getInstance() {
       if (instance == null)  {
           instance = new ECS();
       }
       return instance;
    }

    private RingItem createRingItemFromServer(Server server) {
        String key = new String(hashService.hash(server.toHashableString()), Constants.STRING_CHARSET);
        return new RingItem(key,server);
    }

    @Override
    public void addServer(Server server) {
        RingItem ringItem = createRingItemFromServer(server);
        keyRingService.put(ringItem);
    }

    @Override
    public void deleteServer(Server server) {
        RingItem ringItem = createRingItemFromServer(server);
        keyRingService.delete(ringItem);
    }

    @Override
    public Server getServer(String key) {
        return keyRingService.get(key).value;
    }

    @Override
    public Server getServerForStorageKey(String key) {
        String hashedKey = new String(hashService.hash(key), Constants.STRING_CHARSET);
        return keyRingService.findPredecessor(hashedKey).value;
    }
}
