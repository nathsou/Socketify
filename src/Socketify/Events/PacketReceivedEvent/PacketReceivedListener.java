package Socketify.Events.PacketReceivedEvent;

import java.util.EventListener;

/**
 * Created by nathan on 07/08/15.
 */
public interface PacketReceivedListener extends EventListener{
    void PacketReceived(PacketReceivedEvent packetReceivedEvent);
}
