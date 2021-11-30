package de.tum.i13.ecs.keyring;

import de.tum.i13.ecs.BST.RingNode;
import de.tum.i13.ecs.cs.Server;

public class RingItem {
    public String key;
    public Server value;

    public RingItem(String key, Server value) {
        this.key = key;
        this.value = value;
    }
    public RingItem(){}

    public static RingItem fromRingNode(RingNode ringNode) {
        return new RingItem(ringNode.key, ringNode.value);
    }
}
