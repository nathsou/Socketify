package Socketify.Udp;

import Socketify.Events.CallbackPacketReceivedEvent.CallbackPacketReceivedEvent;
import Socketify.Events.CallbackPacketReceivedEvent.CallbackPacketReceivedListener;
import Socketify.Events.ClientConnectedEvent.ClientConnectedEvent;
import Socketify.Events.ClientConnectedEvent.ClientConnectedListener;
import Socketify.Events.PacketReceivedEvent.PacketReceivedEvent;
import Socketify.Events.PacketReceivedEvent.PacketReceivedListener;
import Socketify.Events.PacketSentEvent.PacketSentEvent;
import Socketify.Events.PacketSentEvent.PacketSentListener;
import Socketify.Packets.Callback;
import Socketify.Packets.CallbackAction;
import Socketify.Packets.Packet;
import Socketify.Socketify.SocketifyServer;

import javax.swing.event.EventListenerList;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

/**
 * Created by nathan on 15/08/15.
 */
public class UdpClient extends Thread {

    private InetAddress host;
    private int port;
    private DatagramSocket socket;
    private int id = -1;
    private EventListenerList listenerList = new EventListenerList();
    private ArrayList<Callback> callbacks;
    private int callbackWaitMs = 50;

    private byte[] buffer = new byte[SocketifyServer.BUFFER_SIZE];

    public UdpClient(String host, int port) throws IOException, ClassNotFoundException {
        this.host = InetAddress.getByName(host);
        this.port = port;
        socket = new DatagramSocket();
        listenerList = new EventListenerList();
        callbacks = new ArrayList<>();
    }


    public void connect() throws IOException {
        //Request client's id to server

        start();

        sendCallBack(new Packet(null, -2, 1), new CallbackAction(this) {
            @Override
            public void run() {
                getSource().setId((int) getServerResponse().getContent());
                fireClientConnectedEvent(new ClientConnectedEvent(this, id));
            }
        }, callbackWaitMs);

    }

    public void disconnect() throws IOException {
        Packet disconnectCallbackPacket = new Packet(null, id, 2);

        sendCallBack(disconnectCallbackPacket, new CallbackAction(this) {
            @Override
            public void run() {
                interrupt();
                socket.close();
            }
        }, callbackWaitMs);

    }

    @Override
    public void run() {
        super.run();

        while (!interrupted()) {

            try {
                DatagramPacket incomingPacket = new DatagramPacket(buffer, buffer.length);
                socket.receive(incomingPacket);
                Packet receivedPacket = Packet.toPacket(incomingPacket.getData());

                if (receivedPacket.getType() != 0) { //Then it's a callback packet

                    int callbackToRemoveIndex = -1;

                    for (Callback cb : callbacks) {
                        if (cb.getType() == receivedPacket.getType()) {
                            cb.setServerResponse(receivedPacket);
                            cb.run();
                            callbackToRemoveIndex = callbacks.indexOf(cb);
                            fireCallbackPacketReceivedEvent(new CallbackPacketReceivedEvent(this, receivedPacket));
                            break;
                        }
                    }

                    if (callbackToRemoveIndex != -1) callbacks.remove(callbackToRemoveIndex);

                } else firePacketReceivedEvent(new PacketReceivedEvent(this, receivedPacket));

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void sendCallBack(Packet cbPacket, CallbackAction action, int waitTimeMs) throws IOException {

        if (cbPacket.getType() < 1)
            throw new IllegalArgumentException("Callback packet's type must be 0 superior than 0.");

        Callback cb = new Callback(this, cbPacket, action);
        callbacks.add(cb);
        byte[] data = Packet.toByteArray(cbPacket);
        socket.send(new DatagramPacket(data, data.length, host, port));

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!interrupted()) {
                    try {
                        Thread.sleep(waitTimeMs);
                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }
                    if (callbacks.contains(cb) && socket.isConnected()) {
                        try {
                            socket.send(new DatagramPacket(data, data.length, host, port));
                        } catch (IOException ioe) {
                            ioe.printStackTrace();
                        }
                    } else {
                        break;
                    }
                }
            }
        }).start();

    }

    public void send(Object obj) throws IOException {
        if (socket != null) {
            byte[] data = Packet.toByteArray(new Packet(obj, id));
            socket.send(new DatagramPacket(data, data.length, host, port));
        }
    }

    //Getters & Setters

    public void setId(int id) {
        this.id = id;
    }

    public boolean isConnected() {
        return id != -1;
    }

    public int getIndex() {
        return id;
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

    //Callback packet received Event

    public void addCallbackPacketReceivedListener(CallbackPacketReceivedListener listener) {
        listenerList.add(CallbackPacketReceivedListener.class, listener);
    }

    public void removeCallbackPacketReceivedListener(CallbackPacketReceivedListener listener) {
        listenerList.remove(CallbackPacketReceivedListener.class, listener);
    }

    private void fireCallbackPacketReceivedEvent(CallbackPacketReceivedEvent event) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == CallbackPacketReceivedListener.class) {
                ((CallbackPacketReceivedListener) listeners[i + 1]).CallbackPacketReceived(event);
            }
        }
    }

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
