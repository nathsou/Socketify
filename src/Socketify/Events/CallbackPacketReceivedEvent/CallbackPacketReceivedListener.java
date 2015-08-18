package Socketify.Events.CallbackPacketReceivedEvent;

import java.util.EventListener;

/**
 * Created by nathan on 08/08/15.
 */
public interface CallbackPacketReceivedListener extends EventListener{
    void CallbackPacketReceived(CallbackPacketReceivedEvent callback);
}
