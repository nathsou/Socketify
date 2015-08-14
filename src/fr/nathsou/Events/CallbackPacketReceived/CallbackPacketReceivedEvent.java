package fr.nathsou.Events.CallbackPacketReceived;

import fr.nathsou.Packets.Packet;

import java.util.EventObject;

/**
 * Created by nathan on 08/08/15.
 */
public class CallbackPacketReceivedEvent extends EventObject{

    private Packet packet;

    public CallbackPacketReceivedEvent(Object source, Packet packet) {
        super(source);
        this.packet = packet;
    }

    //Getters & Setters

    public Packet getPacket() {
        return packet;
    }
}
