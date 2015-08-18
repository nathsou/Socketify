package Socketify.Events.PacketReceivedEvent;

import Socketify.Packets.Packet;
import Socketify.Socketify.ProtocolType;

import java.util.EventObject;

/**
 * Created by nathan on 07/08/15.
 */
public class PacketReceivedEvent extends EventObject{

    private Packet packet;
    private int senderId;
    private ProtocolType protocol = ProtocolType.UNKNOWN;

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

    public ProtocolType getProtocol() {
        return protocol;
    }

    public void setProtocol(ProtocolType protocol) {
        this.protocol = protocol;
    }
}
