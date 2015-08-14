package fr.nathsou.Events.ClientDisconnectedEvent;

import java.util.EventListener;

/**
 * Created by nathan on 08/08/15.
 */
public interface ClientDisconnectedListener extends EventListener {
    void ClientDisconnected(ClientDisconnectedEvent clientDisconnectedEvent);
}
