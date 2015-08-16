package Socketify.Udp;

import Socketify.Events.ClientConnectedEvent.ClientConnectedEvent;
import Socketify.Events.ClientConnectedEvent.ClientConnectedListener;
import Socketify.Events.ClientDisconnectedEvent.ClientDisconnectedEvent;
import Socketify.Events.ClientDisconnectedEvent.ClientDisconnectedListener;
import Socketify.Events.PacketReceivedEvent.PacketReceivedEvent;
import Socketify.Events.PacketReceivedEvent.PacketReceivedListener;
import Socketify.Packets.Packet;
import Socketify.Socketify.SocketifyServer;
import Socketify.Tcp.Client;

import javax.swing.event.EventListenerList;
import java.io.IOException;
import java.net.*;
import java.util.Collection;

/**
 * Created by nathan on 15/08/15.
 */
public class UdpServer extends Thread{

    private int port;
    private DatagramSocket serverSocket;
    private byte[] buffer = new byte[SocketifyServer.BUFFER_SIZE];
    private EventListenerList listenerList = new EventListenerList();
    private int id = 0;

    public UdpServer(int port) throws UnknownHostException{
        this.port = port;
        listenerList = new EventListenerList();
    }

    public void listen(){
        start();
    }

    @Override
    public void run() {
        super.run();

        DatagramPacket incomingPacket;

        while(!interrupted()){

            try {
                if(serverSocket == null) serverSocket = new DatagramSocket(port);
                incomingPacket = new DatagramPacket(buffer, buffer.length);
                try {
                    serverSocket.receive(incomingPacket);
                }catch (SocketException se){
                    break;
                }

                Packet receivedPacket = Packet.toPacket(incomingPacket.getData());

                Packet response;

                switch (receivedPacket.getType()){
                    case 0: //Normal packet
                        firePacketReceivedEvent(new PacketReceivedEvent(this, receivedPacket));
                        break;

                    case 1: //New client callback
                        id++;
                        response = new Packet(id, -1); // -1 -> Server
                        response.setType(1);
                        sendTo(response, incomingPacket.getAddress(), incomingPacket.getPort());

                        SocketifyServer.clients.add(
                                new Client(incomingPacket.getAddress(), receivedPacket.getSenderId(), incomingPacket.getPort())
                        );

                        fireClientConnectedEvent(new ClientConnectedEvent(this, id));
                        break;

                    case 2: //Client disconnected callback

                        for(Client c : SocketifyServer.clients){
                            if(c.getId() == receivedPacket.getSenderId()){
                                SocketifyServer.clients.remove(c);
                            }
                        }

                        sendTo(receivedPacket, incomingPacket.getAddress(), incomingPacket.getPort());

                        break;

                    case 3: //Acknowledgement packet
                        response = new Packet(null, -1);
                        response.setType(3);
                        sendTo(response, incomingPacket.getAddress(), incomingPacket.getPort());
                        firePacketReceivedEvent(new PacketReceivedEvent(this, receivedPacket));
                        break;

                     default:
                         break;
                    }

            }catch (IOException | ClassNotFoundException e){
                e.printStackTrace();
            }

        }

    }

    public void close(){
        interrupt();
        serverSocket.close();
    }

    public synchronized void sendTo(Packet packet, InetAddress address, int port) throws IOException{

        if(serverSocket == null) throw new SocketException("Server must be listening in order to send data.");

        byte[] data = Packet.toByteArray(packet);
        serverSocket.send(new DatagramPacket(data, data.length, address, port));
    }

    public synchronized void sendTo(Object obj, int clientId) throws IOException{
        Client client = getClientById(clientId);

        if(serverSocket == null) throw new SocketException("Server must be listening in order to send data.");

        if(client != null) {
            byte[] data = Packet.toByteArray(new Packet(obj, -1));
            serverSocket.send(new DatagramPacket(data, data.length, client.getAddress(), client.getPort()));
        }else{
            throw new IllegalArgumentException("Client of id: " + clientId + " doesn't exist.");
        }
    }

    public synchronized void sendToAll(Object obj) throws IOException{

        if(serverSocket == null) throw new SocketException("Server must be listening in order to send data.");

        byte[] data = Packet.toByteArray(new Packet(obj, -1));
        for(Client c : SocketifyServer.clients) serverSocket.send(new DatagramPacket(data, data.length, c.getAddress(), c.getPort()));
    }

    public synchronized void sendToAllExcept(Object obj, int exceptId) throws IOException{

        if(serverSocket == null) throw new SocketException("Server must be listening in order to send data.");

        byte[] data = Packet.toByteArray(new Packet(obj, -1));
        for(Client c : SocketifyServer.clients)
            if(c.getId() != exceptId)
                serverSocket.send(new DatagramPacket(data, data.length, c.getAddress(), c.getPort()));
    }

    public synchronized void sendToAllExcept(Object obj, Collection<Integer> exceptIdList) throws IOException{

        if(serverSocket == null) throw new SocketException("Server must be listening in order to send data.");

        byte[] data = Packet.toByteArray(new Packet(obj, -1));
        for(Client c : SocketifyServer.clients)
            if(!exceptIdList.contains(c.getId()))
                serverSocket.send(new DatagramPacket(data, data.length, c.getAddress(), c.getPort()));
    }

    public synchronized void sendToAllExcept(Object obj, int[] exceptIdArray) throws IOException{

        byte[] data = Packet.toByteArray(new Packet(obj, -1));

        for(Client c : SocketifyServer.clients){
            boolean contains = false;
            for(int id : exceptIdArray) if(id == c.getId()) contains = true;
            if(!contains) serverSocket.send(new DatagramPacket(data, data.length, c.getAddress(), c.getPort()));
        }
    }

    private Client getClientById(int id){
        for(Client c : SocketifyServer.clients) if(c.getId() == id) return c;
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


}
