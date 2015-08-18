package Socketify.Events.PacketSentEvent;

import Socketify.Events.PacketReceivedEvent.PacketReceivedEvent;

import java.util.EventListener;

/**
 * Created by Nathan on 18/08/2015.
 */
public interface PacketSentListener extends EventListener{
    void PacketSent(PacketSentEvent packetSentEvent);
}
