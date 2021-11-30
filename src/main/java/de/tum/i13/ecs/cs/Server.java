package de.tum.i13.ecs.cs;

public class Server {
    private String address;
    private String port;

    public Server(String address, String port){
        this.address = address;
        this.port = port;
    }

    public String toHashableString() {
        return address + ":" + port;
    }
}
