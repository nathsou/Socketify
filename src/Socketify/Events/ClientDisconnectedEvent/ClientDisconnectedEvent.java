package Socketify.Events.ClientDisconnectedEvent;

import Socketify.Socketify.ProtocolType;

import java.util.EventObject;

/**
 * Created by nathan on 07/08/15.
 */
public class ClientDisconnectedEvent extends EventObject {

    private int id;
    private ProtocolType protocol = ProtocolType.UNKNOWN;

    public ClientDisconnectedEvent(Object source, int id) {
        super(source);
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public ProtocolType getProtocol() {
        return protocol;
    }

    public void setProtocol(ProtocolType protocol) {
        this.protocol = protocol;
    }
}
