package Socketify.Tcp;

import Socketify.Events.PacketReceivedEvent.PacketReceivedEvent;
import Socketify.Events.PacketReceivedEvent.PacketReceivedListener;
import Socketify.Events.PacketSentEvent.PacketSentEvent;
import Socketify.Events.PacketSentEvent.PacketSentListener;
import Socketify.Packets.Packet;

import javax.swing.event.EventListenerList;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by nathan on 07/08/15.
 */
public class TcpClient {

    private int port;
    private int id;
    private InetAddress host;
    private Socket connection;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private boolean connected = false;
    private EventListenerList listenerList;

    public TcpClient(String host, int port) throws UnknownHostException, IOException {
        this.port = port;
        this.host = InetAddress.getByName(host);
        listenerList = new EventListenerList();
    }

    public void connect() {
        if (!connected) {
            try {

                connection = new Socket(host, port);
                out = new ObjectOutputStream(connection.getOutputStream());
                out.flush();
                in = new ObjectInputStream(connection.getInputStream());
                id = (int) in.readObject();
                connected = true;

                new Thread(new Runnable() { //Receive packets from server
                    @Override
                    public void run() {
                        while (connected) {
                            try {
                                Packet packet = (Packet) in.readObject();
                                firePacketReceivedEvent(new PacketReceivedEvent(this, packet));
                            } catch (ClassNotFoundException cnf) {
                                cnf.printStackTrace();
                            } catch (SocketException se) {
                                //se.printStackTrace();
                            } catch (IOException ioe) {
                                ioe.printStackTrace();
                            }
                        }
                    }
                }).start();

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void disconnect() throws IOException {
        if (connected) {
            out.close();
            in.close();
            connected = false;
            connection.close();
        }
    }

    public void send(Object obj) throws IOException {

        if (connected) {
            //connection.setSoTimeout(2000);
            Packet packet = new Packet(obj, id);
            out.writeObject(packet);
            out.flush();

            firePacketSentEvent(new PacketSentEvent(this, packet));
        } else {
            throw new Error("TClient must be connected to a server in order to send a packet");
        }

    }

    //Getters & Setters
    public boolean isConnected() {
        return connected;
    }

    public int getId() {
        return id;
    }

    public InetAddress getInetAddress() {
        return host;
    }

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
