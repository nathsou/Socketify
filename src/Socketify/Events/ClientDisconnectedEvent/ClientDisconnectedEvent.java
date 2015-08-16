package Socketify.Events.ClientDisconnectedEvent;

import java.util.EventObject;

/**
 * Created by nathan on 07/08/15.
 */
public class ClientDisconnectedEvent extends EventObject {

    private int id;

    public ClientDisconnectedEvent(Object source, int id) {
        super(source);
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
