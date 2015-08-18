package Socketify.Socketify;

import Socketify.Events.ClientConnectedEvent.ClientConnectedEvent;
import Socketify.Events.ClientConnectedEvent.ClientConnectedListener;
import Socketify.Events.PacketReceivedEvent.PacketReceivedEvent;
import Socketify.Events.PacketReceivedEvent.PacketReceivedListener;
import Socketify.Events.PacketSentEvent.PacketSentEvent;
import Socketify.Events.PacketSentEvent.PacketSentListener;
import Socketify.Packets.Packet;
import Socketify.Tcp.TcpClient;
import Socketify.Udp.UdpClient;

import javax.swing.event.EventListenerList;
import java.io.IOException;
import java.net.InetAddress;

/**
 * Created by nathan on 14/08/15.
 */
public class SocketifyClient {

    private InetAddress host;
    private int tcpPort;
    private int udpPort;
    private TcpClient tcpClient;
    private UdpClient udpClient;
    private boolean udpEnabled = false;
    private EventListenerList listenerList;

    public SocketifyClient(String host, int tcpPort, int udpPort) throws IOException, ClassNotFoundException {
        this.host = InetAddress.getByName(host);
        this.tcpPort = tcpPort;
        this.udpPort = udpPort;
        tcpClient = new TcpClient(host, tcpPort);
        udpClient = new UdpClient(host, udpPort);
        udpEnabled = true;
        listenerList = new EventListenerList();
    }

    public SocketifyClient(String host, int tcpPort) throws IOException, ClassNotFoundException {
        this.host = InetAddress.getByName(host);
        this.tcpPort = tcpPort;
        tcpClient = new TcpClient(host, tcpPort);
    }

    public void connect() throws IOException {
        tcpClient.connect();
        if (udpEnabled) udpClient.connect();

        tcpClient.addPacketReceivedListener(new PacketReceivedListener() {
            @Override
            public void PacketReceived(PacketReceivedEvent packetReceivedEvent) {
                packetReceivedEvent.setProtocol(ProtocolType.TCP);
                firePacketReceivedEvent(packetReceivedEvent);
            }
        });

        tcpClient.addPacketSentListener(new PacketSentListener() {
            @Override
            public void PacketSent(PacketSentEvent packetSentEvent) {
                packetSentEvent.setProtocol(ProtocolType.TCP);
                firePacketSentEvent(packetSentEvent);
            }
        });


        udpClient.addPacketReceivedListener(new PacketReceivedListener() {
            @Override
            public void PacketReceived(PacketReceivedEvent packetReceivedEvent) {
                packetReceivedEvent.setProtocol(ProtocolType.UDP);
                firePacketReceivedEvent(packetReceivedEvent);
            }
        });

        udpClient.addPacketSentListener(new PacketSentListener() {
            @Override
            public void PacketSent(PacketSentEvent packetSentEvent) {
                packetSentEvent.setProtocol(ProtocolType.UDP);
                firePacketSentEvent(packetSentEvent);
            }
        });
    }


    public void disconnect() throws IOException {
        tcpClient.disconnect();
        if (udpEnabled) udpClient.disconnect();
    }

    public void send(Object obj, ProtocolType type) throws IOException {

        switch (type) {
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


    //PacketSent Event

    public void addPacketSentListener(PacketSentListener listener) {
        listenerList.add(PacketSentListener.class, listener);
    }

    private void removePacketSentListener(PacketSentListener listener) {
        listenerList.remove(PacketSentListener.class, listener);
    }

    private void firePacketSentEvent(PacketSentEvent event) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == PacketSentListener.class) {
                ((PacketSentListener) listeners[i + 1]).PacketSent(event);
            }
        }
    }

}
