package fr.nathsou.Events.ClientConnectedEvent;

import java.util.EventListener;

/**
 * Created by nathan on 07/08/15.
 */
public interface ClientConnectedListener extends EventListener{
    void ClientConnected(ClientConnectedEvent clientConnectedEvent);
}
