package Socketify.Udp;

import java.net.InetAddress;

/**
 * Created by Nathan on 18/08/2015.
 */
public class UClient {

    private int id;
    private InetAddress address;
    private int port;

    public UClient(InetAddress address, int id, int port){
        this.id = id;
        this.address = address;
        this.port = port;
    }

    //Getters & Setters

    public InetAddress getAddress() {
        return address;
    }

    public int getId() {
        return id;
    }

    public int getPort() {
        return port;
    }

    public boolean equals(UClient client){
        return id == client.getId() && address.equals(client.getAddress()) && port == client.getPort();
    }
}
