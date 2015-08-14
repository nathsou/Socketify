package fr.nathsou.Events.PacketReceivedEvent;

import fr.nathsou.Packets.Packet;

import java.util.EventObject;

/**
 * Created by nathan on 07/08/15.
 */
public class PacketReceivedEvent extends EventObject{

    private Packet packet;
    private int senderId;

    public PacketReceivedEvent(Object source, Packet packet, int senderId) {
        super(source);

        this.packet = packet;
        this.senderId = senderId;
    }

    public Packet getPacket() {
        return packet;
    }

    public int getSenderId() {
        return senderId;
    }
}
