package Socketify.Socketify;

import Socketify.Events.ClientConnectedEvent.ClientConnectedEvent;
import Socketify.Events.ClientConnectedEvent.ClientConnectedListener;
import Socketify.Events.PacketReceivedEvent.PacketReceivedEvent;
import Socketify.Events.PacketReceivedEvent.PacketReceivedListener;
import Socketify.Tcp.TcpClient;
import Socketify.Udp.UdpClient;

import javax.swing.event.EventListenerList;
import java.io.IOException;
import java.net.InetAddress;

/**
 * Created by nathan on 14/08/15.
 */
public class SocketifyClient {

    private int id;
    private InetAddress host;
    private int tcpPort;
    private int udpPort;
    private TcpClient tcpClient;
    private UdpClient udpClient;
    private boolean udpEnabled = false;
    private EventListenerList listenerList;

    public SocketifyClient(String host, int tcpPort, int udpPort) throws IOException, ClassNotFoundException{
        this.host = InetAddress.getByName(host);
        this.tcpPort = tcpPort;
        this.udpPort = udpPort;
        tcpClient = new TcpClient(host, tcpPort);
        udpClient = new UdpClient(host, udpPort);
        udpEnabled = true;
        listenerList = new EventListenerList();
    }

    public SocketifyClient(String host, int tcpPort) throws IOException, ClassNotFoundException{
        this.host = InetAddress.getByName(host);
        this.tcpPort = tcpPort;
        tcpClient = new TcpClient(host, tcpPort);
    }

    public void connect() throws IOException{
        tcpClient.connect();
        if(udpEnabled) udpClient.connect();

        tcpClient.addPacketReceivedListener(new PacketReceivedListener() {
            @Override
            public void PacketReceived(PacketReceivedEvent packetReceivedEvent) {
                packetReceivedEvent.setProtocol(ProtocolType.TCP);
                firePacketReceivedEvent(packetReceivedEvent);
            }
        });

        udpClient.addPacketReceivedListener(new PacketReceivedListener() {
            @Override
            public void PacketReceived(PacketReceivedEvent packetReceivedEvent) {
                packetReceivedEvent.setProtocol(ProtocolType.UDP);
                firePacketReceivedEvent(packetReceivedEvent);
            }
        });
    }


    public void disconnect() throws IOException{
        tcpClient.disconnect();
        if(udpEnabled) udpClient.disconnect();
    }

    public void send(Object obj, ProtocolType type) throws IOException{
        switch (type){
            case TCP:
                tcpClient.send(obj);
                break;
            case UDP:
                udpClient.send(obj);
                break;
        }
    }

    //Events

    //PacketReceived Event

    public void addPacketReceivedListener(PacketReceivedListener listener) {
        listenerList.add(PacketReceivedListener.class, listener);
    }

    public void removePacketReceivedListener(PacketReceivedListener listener) {
        listenerList.remove(PacketReceivedListener.class, listener);
    }

    private void firePacketReceivedEvent(PacketReceivedEvent event) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == PacketReceivedListener.class) {
                ((PacketReceivedListener) listeners[i + 1]).PacketReceived(event);
            }
        }
    }

}
