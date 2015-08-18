package Socketify.Events.PacketSentEvent;

import Socketify.Packets.Packet;
import Socketify.Socketify.ProtocolType;

import java.util.EventObject;

/**
 * Created by Nathan on 18/08/2015.
 */
public class PacketSentEvent extends EventObject{

    private Packet packet;
    private int senderId;
    private ProtocolType protocol = ProtocolType.UNKNOWN;

    public PacketSentEvent(Object source, Packet packet) {
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
