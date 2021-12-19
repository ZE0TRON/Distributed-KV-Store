package de.tum.i13.ecs.cs;

import de.tum.i13.shared.Pair;
import de.tum.i13.shared.Server;

public interface ConfigurationService {

    void addServer(Server server);
    void deleteServer(Server server);
    Server getServer(String key);
    void updateMetadata();
    boolean handoverFinished(Pair<String, String> keyRange);
    Server getServerForStorageKey(String key);
}
