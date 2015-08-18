package Socketify.Udp;

import Socketify.Events.ClientConnectedEvent.ClientConnectedEvent;
import Socketify.Events.ClientConnectedEvent.ClientConnectedListener;
import Socketify.Events.ClientDisconnectedEvent.ClientDisconnectedEvent;
import Socketify.Events.ClientDisconnectedEvent.ClientDisconnectedListener;
import Socketify.Events.PacketReceivedEvent.PacketReceivedEvent;
import Socketify.Events.PacketReceivedEvent.PacketReceivedListener;
import Socketify.Events.PacketSentEvent.PacketSentEvent;
import Socketify.Events.PacketSentEvent.PacketSentListener;
import Socketify.Packets.Packet;
import Socketify.Socketify.SocketifyServer;

import javax.swing.event.EventListenerList;
import java.io.IOException;
import java.net.*;
import java.util.Collection;

/**
 * Created by nathan on 15/08/15.
 */
public class UdpServer extends Thread {

    private int port;
    private DatagramSocket serverSocket;
    private byte[] buffer = new byte[SocketifyServer.BUFFER_SIZE];
    private EventListenerList listenerList = new EventListenerList();
    private int id = 0;

    public UdpServer(int port) throws UnknownHostException {
        this.port = port;
        listenerList = new EventListenerList();
    }

    public void listen() {
        start();
    }

    @Override
    public void run() {
        super.run();

        DatagramPacket incomingPacket;

        while (!interrupted()) {

            try {
                if (serverSocket == null) serverSocket = new DatagramSocket(port);
                incomingPacket = new DatagramPacket(buffer, buffer.length);
                try {
                    serverSocket.receive(incomingPacket);
                } catch (SocketException se) {
                    break;
                }

                Packet receivedPacket = Packet.toPacket(incomingPacket.getData());

                Packet response;

                switch (receivedPacket.getType()) {
                    case 0: //Normal packet
                        firePacketReceivedEvent(new PacketReceivedEvent(this, receivedPacket));
                        break;

                    case 1: //New client callback
                        id++;
                        response = new Packet(id, -1, 1); // -1 -> Server
                        sendTo(response, incomingPacket.getAddress(), incomingPacket.getPort());

                        addClient(incomingPacket.getAddress(), incomingPacket.getPort(), id);

                        fireClientConnectedEvent(new ClientConnectedEvent(this, id));
                        break;

                    case 2: //UClient disconnected callback

                        for (UClient c : SocketifyServer.UClients) {
                            if (c.getId() == receivedPacket.getSenderId()) {
                                sendTo(receivedPacket, incomingPacket.getAddress(), incomingPacket.getPort());
                                removeClient(c);
                                fireClientDisconnectedEvent(new ClientDisconnectedEvent(this, c.getId()));
                                break;
                            }
                        }

                        break;

                    case 3: //Acknowledgement packet
                        response = new Packet(null, -1, 3);
                        sendTo(response, incomingPacket.getAddress(), incomingPacket.getPort());
                        firePacketReceivedEvent(new PacketReceivedEvent(this, receivedPacket));
                        break;

                    default:
                        sendTo(receivedPacket, incomingPacket.getAddress(), incomingPacket.getPort());
                        break;
                }

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }

        }
    }

    public void close() {
        interrupt();
        if (serverSocket.isConnected()) serverSocket.close();
    }

    private void addClient(InetAddress address, int port, int senderId) throws IOException {

        for (UClient c : SocketifyServer.UClients) if (c.getAddress().equals(address) && c.getPort() == port) return;
        SocketifyServer.UClients.add(new UClient(address, senderId, port));
    }

    private void addClient(UClient client) throws IOException {
        for (UClient c : SocketifyServer.UClients) if (c.equals(client)) return;
        SocketifyServer.UClients.add(client);
    }

    private void removeClient(InetAddress address, int port, int senderId) throws IOException {
        for (UClient c : SocketifyServer.UClients)
            if (c.getAddress().equals(address) && c.getPort() == port) {
                SocketifyServer.UClients.remove(c);
                break;
            }
    }

    private void removeClient(UClient client) throws IOException {
        for (UClient c : SocketifyServer.UClients) {
            if (c.equals(client)) {
                SocketifyServer.UClients.remove(client);
                break;
            }
        }
    }

    public void sendTo(Packet packet, InetAddress address, int port) throws IOException {

        if (serverSocket == null) throw new SocketException("Server must be listening in order to send data.");

        byte[] data = Packet.toByteArray(packet);
        serverSocket.send(new DatagramPacket(data, data.length, address, port));
        firePacketSentEvent(new PacketSentEvent(this, packet));
    }

    public void sendTo(Object obj, int clientId) throws IOException {
        UClient client = geUClientById(clientId);

        if (serverSocket == null) throw new SocketException("Server must be listening in order to send data.");

        if (client != null) {
            Packet packet = new Packet(obj, -1);
            byte[] data = Packet.toByteArray(packet);
            serverSocket.send(new DatagramPacket(data, data.length, client.getAddress(), client.getPort()));
            firePacketSentEvent(new PacketSentEvent(this, packet));
        } else {
            throw new IllegalArgumentException("UClient of id: " + clientId + " doesn't exist.");
        }
    }

    public void sendToAll(Object obj) throws IOException {

        if (serverSocket == null) throw new SocketException("Server must be listening in order to send data.");

        Packet packet = new Packet(obj, -1);
        byte[] data = Packet.toByteArray(packet);
        for (UClient c : SocketifyServer.UClients) {
            serverSocket.send(new DatagramPacket(data, data.length, c.getAddress(), c.getPort()));
            firePacketSentEvent(new PacketSentEvent(this, packet));
        }
    }

    public void sendToAllExcept(Object obj, int exceptId) throws IOException {

        if (serverSocket == null) throw new SocketException("Server must be listening in order to send data.");

        Packet packet = new Packet(obj, -1);
        byte[] data = Packet.toByteArray(packet);
        for (UClient c : SocketifyServer.UClients)
            if (c.getId() != exceptId) {
                serverSocket.send(new DatagramPacket(data, data.length, c.getAddress(), c.getPort()));
                firePacketSentEvent(new PacketSentEvent(this, packet));
            }
    }

    public void sendToAllExcept(Object obj, Collection<Integer> exceptIdList) throws IOException {

        if (serverSocket == null) throw new SocketException("Server must be listening in order to send data.");

        Packet packet = new Packet(obj, -1);
        byte[] data = Packet.toByteArray(packet);
        for (UClient c : SocketifyServer.UClients)
            if (!exceptIdList.contains(c.getId())) {
                serverSocket.send(new DatagramPacket(data, data.length, c.getAddress(), c.getPort()));
                firePacketSentEvent(new PacketSentEvent(this, packet));
            }
    }

    public void sendToAllExcept(Object obj, int[] exceptIdArray) throws IOException {

        Packet packet = new Packet(obj, -1);
        byte[] data = Packet.toByteArray(packet);

        for (UClient c : SocketifyServer.UClients) {
            boolean contains = false;
            for (int id : exceptIdArray) if (id == c.getId()) contains = true;
            if (!contains) {
                serverSocket.send(new DatagramPacket(data, data.length, c.getAddress(), c.getPort()));
                firePacketSentEvent(new PacketSentEvent(this, packet));
            }
        }
    }

    private UClient geUClientById(int id) {
        for (UClient c : SocketifyServer.UClients) if (c.getId() == id) return c;
        return null;
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
