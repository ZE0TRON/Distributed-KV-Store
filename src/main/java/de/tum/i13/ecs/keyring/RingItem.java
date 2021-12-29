package de.tum.i13.ecs.keyring;

import de.tum.i13.shared.BST.RingNode;
import de.tum.i13.shared.Server;

public class RingItem {
    public String key;
    public Server value;

    public RingItem(String key, Server value) {
        this.key = key;
        this.value = value;
    }
    public RingItem(){}

    public static RingItem fromRingNode(RingNode ringNode) {
        if (ringNode == null) {
            return null;
        }
        return new RingItem(ringNode.key, ringNode.value);
    }
    public static RingItem createRingItemFromServer(Server server) {
        String key = Server.serverToHashString(server);
        return new RingItem(key,server);
    }


}
