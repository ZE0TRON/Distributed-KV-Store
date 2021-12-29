package de.tum.i13.shared;

import de.tum.i13.ecs.keyring.ConsistentHashingService;
import de.tum.i13.ecs.keyring.HashService;

import java.security.NoSuchAlgorithmException;

public class Server {
    private String address;
    private String port;
    private String ecsPort;

    public Server(String address, String port){
        this.address = address;
        this.port = port;
    }

    public Server(String address, String port, String ecsPort) {
        this.address = address;
        this.port = port;
        this.ecsPort = ecsPort;
    }

    public String toHashableString() {
        return address + ":" + port;
    }

    public String toEcsConnectionString() {
        return address + ":" + ecsPort;
    }


    public static Server fromHashableString(String hashableString) {
        String[] parts = hashableString.split(":");
        String address = parts[0];
        String port = parts[1];
        return new Server(address,port);
    }

    public static String serverToHashString(Server server) {
        try {
            HashService hashService = new ConsistentHashingService(Constants.HASHING_ALGORITHM);
            return ConsistentHashingService.byteHashToStringHash(hashService.hash(server.toHashableString()));
        } catch (NoSuchAlgorithmException e) {
            // TODO log
            return null;
        }
    }

    public String getAddress() {
        return address;
    }

    public String getPort() {
        return port;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getEcsPort() {
        return ecsPort;
    }

    public void setEcsPort(String ecsPort) {
        this.ecsPort = ecsPort;
    }
}
