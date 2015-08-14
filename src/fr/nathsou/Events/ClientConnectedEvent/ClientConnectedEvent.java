package fr.nathsou.Events.ClientConnectedEvent;

import java.util.EventObject;

/**
 * Created by nathan on 07/08/15.
 */
public class ClientConnectedEvent extends EventObject{

    private int id;

    public ClientConnectedEvent(Object source, int id) {
        super(source);
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
