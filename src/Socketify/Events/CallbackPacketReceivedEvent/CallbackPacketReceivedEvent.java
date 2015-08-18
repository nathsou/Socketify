package Socketify.Events.CallbackPacketReceivedEvent;

import Socketify.Packets.Packet;
import Socketify.Socketify.ProtocolType;

import java.util.EventObject;

/**
 * Created by nathan on 08/08/15.
 */
public class CallbackPacketReceivedEvent extends EventObject{

    private Packet packet;
    private ProtocolType protocol = ProtocolType.UNKNOWN;

    public CallbackPacketReceivedEvent(Object source, Packet packet) {
        super(source);
        this.packet = packet;
    }

    //Getters & Setters

    public Packet getPacket() {
        return packet;
    }

    public ProtocolType getProtocol() {
        return protocol;
    }

    public void setProtocol(ProtocolType protocol) {
        this.protocol = protocol;
    }
}
