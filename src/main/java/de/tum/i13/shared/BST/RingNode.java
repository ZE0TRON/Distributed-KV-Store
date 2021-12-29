package de.tum.i13.shared.BST;

import de.tum.i13.shared.Server;
import de.tum.i13.ecs.keyring.RingItem;

public class RingNode {
    public String key;
    public Server value;

    public RingNode left;
    public RingNode right;
    public RingNode parent;

    RingNode(String key, Server value, RingNode left, RingNode right, RingNode parent) {
        this.key = key;
        this.value = value;
        this.left = left;
        this.right = right;
        this.parent = parent;
    }

    RingNode(String key, Server value, RingNode parent) {
        this.key = key;
        this.value = value;
        this.left = null;
        this.right = null;
        this.parent = parent;
    }
    RingNode(String key, Server value) {
        this.key = key;
        this.value = value;
        this.left = null;
        this.right = null;
        this.parent = null;
    }

    public static RingNode fromRingItem(RingItem ringItem) {
        return new RingNode(ringItem.key, ringItem.value);
    }

    public String serialize() {
        return key +
                ";" +
                value.toHashableString();
    }

    public static RingNode deserialize(String data) {
        String[] parts = data.split(";");
        String key = parts[0];
        Server server = Server.fromHashableString(parts[1]);
        return new RingNode(key, server);
    }
}
