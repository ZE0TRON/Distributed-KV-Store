package de.tum.i13.ecs.keyring;

import de.tum.i13.ecs.cs.Server;

public interface ConfigurationService {

    public void addServer(Server server);
    public void deleteServer(Server server);
    public Server getServer(String key);

    public Server getServerForStorageKey(String key);
}
