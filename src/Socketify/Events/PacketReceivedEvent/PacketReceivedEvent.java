package Socketify.Events.PacketReceivedEvent;

import Socketify.Packets.Packet;

import java.util.EventObject;

/**
 * Created by nathan on 07/08/15.
 */
public class PacketReceivedEvent extends EventObject{

    private Packet packet;
    private int senderId;

    public PacketReceivedEvent(Object source, Packet packet) {
        super(source);

        this.packet = packet;
        senderId = packet.getSenderId();
    }

    public Packet getPacket() {
        return packet;
    }

    public int getSenderId() {
        return senderId;
    }
}
