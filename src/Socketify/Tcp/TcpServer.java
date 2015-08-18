package Socketify.Tcp;

import Socketify.Events.ClientConnectedEvent.ClientConnectedListener;
import Socketify.Events.PacketReceivedEvent.PacketReceivedListener;
import Socketify.Events.ClientConnectedEvent.ClientConnectedEvent;
import Socketify.Events.ClientDisconnectedEvent.ClientDisconnectedEvent;
import Socketify.Events.ClientDisconnectedEvent.ClientDisconnectedListener;
import Socketify.Events.PacketReceivedEvent.PacketReceivedEvent;
import Socketify.Events.PacketSentEvent.PacketSentEvent;
import Socketify.Events.PacketSentEvent.PacketSentListener;
import Socketify.Packets.Packet;
import Socketify.Socketify.ProtocolType;
import Socketify.Socketify.SocketifyServer;

import javax.swing.event.EventListenerList;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nathan on 07/08/15.
 */
public class TcpServer extends Thread {

    private int port;
    private ServerSocket serverSocket;
    private int id = 0;
    private Socket connection;
    private EventListenerList listenerList = new EventListenerList();

    public TcpServer(int port) {

        this.port = port;
        try {
            serverSocket = new ServerSocket(port);
            //serverSocket.setSoTimeout(2000);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        SocketifyServer.TClients = new ArrayList<>();
    }

    public void listen() {
        start();
    }

    @Override
    public void run() {
        super.run();

        try {

            while (true) {
                connection = serverSocket.accept();
                TClient client = new TClient(connection, id + 1);
                if (!clientExists(client)) {
                    id++;
                    addClient(client);
                }
                client.writeObject(id);
                fireClientConnectedEvent(new ClientConnectedEvent(this, id));

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        while (true) {
                            try {
                                Packet packet = (Packet) client.readObject();

                                switch (packet.getType()) {
                                    case 0:
                                        firePacketReceivedEvent(new PacketReceivedEvent(this, packet));
                                        break;
                                    default:
                                        break;
                                }

                            } catch (ClassNotFoundException cnf) {
                                cnf.printStackTrace();
                            } catch (IOException ioe) {
                                try {
                                    client.close();
                                } catch (IOException ioe2) {
                                    ioe2.printStackTrace();
                                }
                                SocketifyServer.TClients.remove(client);
                                fireClientDisconnectedEvent(new ClientDisconnectedEvent(this, client.getId()));
                                break;
                            }
                        }
                    }
                }).start();

            }

        } catch (SocketException se) {

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void sendTo(Object obj, int clientId) throws IOException {
        for (TClient c : SocketifyServer.TClients) {
            if (c.getId() == clientId) {
                Packet packet = new Packet(obj, -1);
                c.writeObject(packet);
                firePacketSentEvent(new PacketSentEvent(this, packet));
                break;
            }
        }
    }

    public void sendToAll(Object obj) throws IOException {
        for (TClient c : SocketifyServer.TClients) {
            Packet packet = new Packet(obj, -1);
            c.writeObject(packet);
            firePacketSentEvent(new PacketSentEvent(this, packet));
        }
    }

    public void sendToAllExcept(Object obj, int exceptId) throws IOException {
        for (TClient c : SocketifyServer.TClients)
            if (c.getId() != exceptId) {
                Packet packet = new Packet(obj, -1);
                c.writeObject(packet);
                firePacketSentEvent(new PacketSentEvent(this, packet));
            }
    }

    public void sendToAllExcept(Object obj, List<Integer> exceptIds) throws IOException {
        for (TClient c : SocketifyServer.TClients)
            if (!exceptIds.contains(c.getId())) {
                Packet packet = new Packet(obj, -1);
                c.writeObject(packet);
                firePacketSentEvent(new PacketSentEvent(this, packet));
            }
    }

    public void sendToAllExcept(Object obj, int[] exceptIds) throws IOException {

        for (TClient c : SocketifyServer.TClients) {
            boolean contains = false;
            for (int id : exceptIds)
                if (id == c.getId()) {
                    contains = true;
                }

            if (!contains) {
                Packet packet = new Packet(obj, -1);
                c.writeObject(packet);
                firePacketSentEvent(new PacketSentEvent(this, packet));
            }
        }
    }


    public void close() {
        interrupt();
        try {
            if (!serverSocket.isClosed()) {
                serverSocket.close();
                id = 0;
            }

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void addClient(TClient client) {
        if (!clientExists(client))
            SocketifyServer.TClients.add(client);
    }


    private boolean clientExists(TClient client) {
        for (TClient c : SocketifyServer.TClients) if (c.equals(client)) return true;
        return false;
    }

    //Getters && Setters

    public InetAddress getInetAddress() {
        return serverSocket.getInetAddress();
    }

    public int getPort() {
        return port;
    }

    public int clientCount() {
        return SocketifyServer.TClients.size();
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
