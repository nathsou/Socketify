package Socketify.Packets;

import Socketify.Udp.UdpClient;

/**
 * Created by nathan on 16/08/15.
 */
public class CallbackAction implements Runnable {

    private static Packet serverResponse;
    private static UdpClient source;

    public CallbackAction(final UdpClient source){
        this.source = source;
    }

    public void setServerResponse(final Packet serverResponse) {
        this.serverResponse = serverResponse;
    }

    @Override
    public void run() {

    }

    //Getters & Setters

    public static UdpClient getSource() {
        return source;
    }

    public static Packet getServerResponse() {
        return serverResponse;
    }
}
