package de.tum.i13.server.kv;

public class KVCommandProcessor implements de.tum.i13.shared.CommandProcessor {
    private CommandProcessor(KVStore kvStore) {
        this.kvStore = kvStore;
    }
}
