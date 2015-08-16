package Socketify.Packets;

import Socketify.Udp.UdpClient;

/**
 * Created by nathan on 16/08/15.
 */
public class Callback {

    private Packet packet;
    private CallbackAction action;

    public Callback(UdpClient source, Packet packet, CallbackAction action) {
        this.action = action;
        this.packet = packet;
    }

    public void run() {
        action.run();
    }

    //Getters & Setters


    public Packet getPacket() {
        return packet;
    }

    public short getType() {
        return packet.getType();
    }

    public void setServerResponse(Packet serverResponse) {
        action.setServerResponse(serverResponse);
    }

    public Runnable getAction() {
        return action;
    }
}
