package Socketify.Socketify;

import Socketify.Events.ClientConnectedEvent.ClientConnectedEvent;
import Socketify.Events.ClientConnectedEvent.ClientConnectedListener;
import Socketify.Events.ClientDisconnectedEvent.ClientDisconnectedEvent;
import Socketify.Events.ClientDisconnectedEvent.ClientDisconnectedListener;
import Socketify.Events.PacketReceivedEvent.PacketReceivedEvent;
import Socketify.Events.PacketReceivedEvent.PacketReceivedListener;
import Socketify.Events.PacketSentEvent.PacketSentEvent;
import Socketify.Events.PacketSentEvent.PacketSentListener;
import Socketify.Tcp.TClient;
import Socketify.Tcp.TcpServer;
import Socketify.Udp.UClient;
import Socketify.Udp.UdpServer;

import javax.swing.event.EventListenerList;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Nathan on 12/08/2015.
 */
public class SocketifyServer {

    public static final int BUFFER_SIZE = 65536;
    public static ArrayList<TClient> TClients = new ArrayList<>();
    public static ArrayList<UClient> UClients = new ArrayList<>();

    private TcpServer tcpServer;
    private UdpServer udpServer;
    private boolean udpEnabled = false;

    private EventListenerList listenerList;

    public SocketifyServer(int tcpPort, int udpPort) throws UnknownHostException {
        tcpServer = new TcpServer(tcpPort);
        udpServer = new UdpServer(udpPort);
        udpEnabled = true;
        listenerList = new EventListenerList();
    }

    public SocketifyServer(int tcpPort) throws UnknownHostException {
        tcpServer = new TcpServer(tcpPort);
    }

    public void listen() {
        tcpServer.listen();

        tcpServer.addClientConnectedListener(new ClientConnectedListener() {
            @Override
            public void ClientConnected(ClientConnectedEvent clientConnectedEvent) {
                clientConnectedEvent.setProtocol(ProtocolType.TCP);
                fireClientConnectedEvent(clientConnectedEvent);
            }
        });

        tcpServer.addPacketReceivedListener(new PacketReceivedListener() {
            @Override
            public void PacketReceived(PacketReceivedEvent packetReceivedEvent) {
                packetReceivedEvent.setProtocol(ProtocolType.TCP);
                firePacketReceivedEvent(packetReceivedEvent);
            }
        });

        tcpServer.addClientDisconnectedListener(new ClientDisconnectedListener() {
            @Override
            public void ClientDisconnected(ClientDisconnectedEvent clientDisconnectedEvent) {
                clientDisconnectedEvent.setProtocol(ProtocolType.TCP);
                fireClientDisconnectedEvent(clientDisconnectedEvent);
            }
        });

        tcpServer.addPacketSentListener(new PacketSentListener() {
            @Override
            public void PacketSent(PacketSentEvent packetSentEvent) {
                packetSentEvent.setProtocol(ProtocolType.TCP);
                firePacketSentEvent(packetSentEvent);
            }
        });

        if (udpEnabled) {
            udpServer.listen();

            udpServer.addClientConnectedListener(new ClientConnectedListener() {
                @Override
                public void ClientConnected(ClientConnectedEvent clientConnectedEvent) {
                    clientConnectedEvent.setProtocol(ProtocolType.UDP);
                    fireClientConnectedEvent(clientConnectedEvent);
                }
            });

            udpServer.addPacketReceivedListener(new PacketReceivedListener() {
                @Override
                public void PacketReceived(PacketReceivedEvent packetReceivedEvent) {
                    packetReceivedEvent.setProtocol(ProtocolType.UDP);
                    firePacketReceivedEvent(packetReceivedEvent);
                }
            });

            udpServer.addClientDisconnectedListener(new ClientDisconnectedListener() {
                @Override
                public void ClientDisconnected(ClientDisconnectedEvent clientDisconnectedEvent) {
                    clientDisconnectedEvent.setProtocol(ProtocolType.UDP);
                    fireClientDisconnectedEvent(clientDisconnectedEvent);
                }
            });

            udpServer.addPacketSentListener(new PacketSentListener() {
                @Override
                public void PacketSent(PacketSentEvent packetSentEvent) {
                    packetSentEvent.setProtocol(ProtocolType.UDP);
                    firePacketSentEvent(packetSentEvent);
                }
            });
        }
    }

    public void close() {
        tcpServer.close();
        if (udpEnabled) udpServer.close();
    }

    public void sendToAll(Object obj, ProtocolType type) throws IOException {
        switch (type) {
            case TCP:
                tcpServer.sendToAll(obj);
                break;
            case UDP:
                udpServer.sendToAll(obj);
                break;
            default:
                throw new IllegalArgumentException("Invalid type argument.");
        }
    }

    public void sendTo(Object obj, int clientId, ProtocolType type) throws IOException {
        switch (type) {
            case TCP:
                tcpServer.sendTo(obj, clientId);
                break;
            case UDP:
                udpServer.sendTo(obj, clientId);
                break;
            default:
                throw new IllegalArgumentException("Invalid type argument.");
        }
    }

    public void sendToAllExcept(Object obj, int exceptId, ProtocolType type) throws IOException {
        switch (type) {
            case TCP:
                tcpServer.sendToAllExcept(obj, exceptId);
                break;
            case UDP:
                udpServer.sendToAllExcept(obj, exceptId);
                break;
            default:
                throw new IllegalArgumentException("Invalid type argument.");
        }
    }

    public void sendToAllExcept(Object obj, int[] exceptIds, ProtocolType type) throws IOException {
        switch (type) {
            case TCP:
                tcpServer.sendToAllExcept(obj, exceptIds);
                break;
            case UDP:
                udpServer.sendToAllExcept(obj, exceptIds);
                break;
            default:
                throw new IllegalArgumentException("Invalid type argument.");
        }
    }


    public void sendToAllExcept(Object obj, Collection<Integer> exceptIds, ProtocolType type) throws IOException {
        switch (type) {
            case TCP:
                tcpServer.sendToAllExcept(obj, exceptIds);
                break;
            case UDP:
                udpServer.sendToAllExcept(obj, exceptIds);
                break;
            default:
                throw new IllegalArgumentException("Invalid type argument.");
        }
    }

    //Getters & Setters

    public TcpServer getTcpServer() {
        return tcpServer;
    }

    public UdpServer getUdpServer() {
        return udpServer;
    }

    //Events

    //ClientConnected Event

    public void addClientConnectedListener(ClientConnectedListener listener) {
        listenerList.add(ClientConnectedListener.class, listener);
    }

    private void removeClientConnectedListener(ClientConnectedListener listener) {
        listenerList.remove(ClientConnectedListener.class, listener);
    }

    private void fireClientConnectedEvent(ClientConnectedEvent event) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == ClientConnectedListener.class) {
                ((ClientConnectedListener) listeners[i + 1]).ClientConnected(event);
            }
        }
    }

    //ClientDisconnected Event

    public void addClientDisconnectedListener(ClientDisconnectedListener listener) {
        listenerList.add(ClientDisconnectedListener.class, listener);
    }

    private void removeClientDisconnectedListener(ClientDisconnectedListener listener) {
        listenerList.remove(ClientDisconnectedListener.class, listener);
    }

    private void fireClientDisconnectedEvent(ClientDisconnectedEvent event) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == ClientDisconnectedListener.class) {
                ((ClientDisconnectedListener) listeners[i + 1]).ClientDisconnected(event);
            }
        }
    }

    //PacketReceived Event

    public void addPacketReceivedListener(PacketReceivedListener listener) {
        listenerList.add(PacketReceivedListener.class, listener);
    }

    private void removePacketReceivedListener(PacketReceivedListener listener) {
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
