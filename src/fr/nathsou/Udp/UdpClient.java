package fr.nathsou.Udp;

import fr.nathsou.Events.CallbackPacketReceived.CallbackPacketReceivedEvent;
import fr.nathsou.Events.CallbackPacketReceived.CallbackPacketReceivedListener;
import fr.nathsou.Events.PacketReceivedEvent.PacketReceivedEvent;
import fr.nathsou.Events.PacketReceivedEvent.PacketReceivedListener;
import fr.nathsou.Packets.Packet;
import fr.nathsou.Socketify.SocketifyServer;

import javax.swing.event.EventListenerList;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Created by nathan on 07/08/15.
 */
public class UdpClient {

    private InetAddress host;
    private int port;
    private DatagramSocket socket;
    private byte[] buffer = new byte[SocketifyServer.BUFFER_SIZE];
    private EventListenerList listenerList;
    private boolean canCommunicate = false;
    private boolean received = false;

    public UdpClient(String host, int port) throws IOException, ClassNotFoundException{
        this.host = InetAddress.getByName(host);
        this.port = port;
        socket = new DatagramSocket();
        listenerList = new EventListenerList();
    }

    public void connect(){

        socket.connect(this.host, port);
        canCommunicate = true;

        new Thread(new Runnable() {
            @Override
            public void run() {
                while(canCommunicate) {
                    try {
                        DatagramPacket datagramPacket = new DatagramPacket(buffer, SocketifyServer.BUFFER_SIZE);
                        socket.receive(datagramPacket);
                        Packet packet = Packet.toPacket(buffer);
                        if(packet.getType() != 0){
                            fireCallbackPacketReceivedEvent(new CallbackPacketReceivedEvent(this, packet));
                        }else{
                            firePacketReceivedEvent(new PacketReceivedEvent(this, packet, 0));
                        }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }catch (IOException ioe){
                        if(ioe instanceof SocketException){
                            break;
                        }else{
                            ioe.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }

    public void send(Packet packet) throws IOException{
        byte[] packetByteArray = Packet.toByteArray(packet);
        socket.send(new DatagramPacket(packetByteArray, packetByteArray.length));
    }

    private void sendCallBack(Packet packet) throws IOException{ //Preferably not to be used

        if(packet.getType() != 0){
            received = false;

            CallbackPacketReceivedListener listener = new CallbackPacketReceivedListener() {
                @Override
                public void CallbackPacketReceived(CallbackPacketReceivedEvent callback) {
                    System.out.println("Callback packet received");
                    if (callback.getPacket().getSenderId() == packet.getSenderId()) {
                        received = true;
                        System.out.println("Callback packet received");
                    }
                }
            };

            addCallbackPacketReceivedListener(listener);

            new Thread(new Runnable() {
                @Override
                public void run() {

                    while(!received){
                        System.out.println("New callback packet sent");
                        try {
                            send(packet);
                        }catch (IOException e){e.printStackTrace();}
                    }

                }
            }).start();

            removeCallbackPacketReceivedListener(listener);
            received = false;

        }else{
            throw new IllegalArgumentException("Packets sent with callback must have a specific type.");
        }

    }

    public void closeSocket(){
        socket.close();
        canCommunicate = false;
    }

    //Getters & Setters

    public InetAddress getInetAddress() {
        return host;
    }

    public int getPort() {
        return port;
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

    //CallbackPacketReceived Event

    private void addCallbackPacketReceivedListener(CallbackPacketReceivedListener listener) {
        listenerList.add(CallbackPacketReceivedListener.class, listener);
    }

    private void removeCallbackPacketReceivedListener(CallbackPacketReceivedListener listener) {
        listenerList.remove(CallbackPacketReceivedListener.class, listener);
    }

    private void fireCallbackPacketReceivedEvent(CallbackPacketReceivedEvent event) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == PacketReceivedListener.class) {
                ((CallbackPacketReceivedListener) listeners[i + 1]).CallbackPacketReceived(event);
            }
        }
    }
}

