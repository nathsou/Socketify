package fr.nathsou.Udp;

import fr.nathsou.Events.ClientConnectedEvent.ClientConnectedEvent;
import fr.nathsou.Events.ClientConnectedEvent.ClientConnectedListener;
import fr.nathsou.Events.ClientDisconnectedEvent.ClientDisconnectedEvent;
import fr.nathsou.Events.ClientDisconnectedEvent.ClientDisconnectedListener;
import fr.nathsou.Events.PacketReceivedEvent.PacketReceivedEvent;
import fr.nathsou.Events.PacketReceivedEvent.PacketReceivedListener;
import fr.nathsou.Packets.Packet;
import fr.nathsou.Socketify.SocketifyServer;
import fr.nathsou.Tcp.Client;

import javax.swing.event.EventListenerList;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nathan on 07/08/15.
 */
public class UdpServer extends Thread{

    private DatagramSocket serverSocket;
    private byte[] buffer = new byte[SocketifyServer.BUFFER_SIZE];
    private EventListenerList listenerList;
    private int port;
    private int id = 0;
    private boolean listening = false;

    public UdpServer(int port){
        this.port = port;
        listenerList = new EventListenerList();
    }

    public void listen(){
        start();
    }

    @Override
    public void run() {
        super.run();

        DatagramPacket datagramPacket;

        try {
            serverSocket = new DatagramSocket(port);

            while (!isInterrupted()) {
                listening = true;
                datagramPacket = new DatagramPacket(buffer, SocketifyServer.BUFFER_SIZE);
                serverSocket.receive(datagramPacket);
                Packet packet = Packet.toPacket(buffer);

                switch (packet.getType()){
                    case 0: //Normal packet
                        int senderId = addClient(datagramPacket.getAddress(), packet.getSenderId());
                        firePacketReceivedEvent(new PacketReceivedEvent(this, packet, senderId));
                        break;
                    case 1: //New client callback packet
                        System.out.println("New client connected callback packet received");
                        sendTo(packet, addClient(datagramPacket.getAddress(), packet.getSenderId()));
                        break;
                    case 2: //Client disconnected callback packet
                        System.out.println("New client disconnected callback packet received");
                        removeClient(packet.getSenderId());
                        sendTo(packet, packet.getSenderId());
                    default:
                        break;
                }
            }
        }catch (SocketException se){
            //Server closed
        }catch (IOException ioe){
            ioe.printStackTrace();
        }catch (ClassNotFoundException cnf){
            cnf.printStackTrace();
        }
    }

    private int addClient(InetAddress address, int id){

        if(clientExists(id)){
            id++;
            try {
                SocketifyServer.clients.add(new Client(address, id, port));
            }catch (IOException ioe){
                ioe.printStackTrace();
            }
            fireClientConnectedEvent(new ClientConnectedEvent(this, id));
            return id - 1;
        }

        return id - 1;
    }

    private boolean clientExists(int id){
        for(Client c : SocketifyServer.clients) if(c.getId() == id) return true;
        return false;
    }

    private boolean removeClient(int id){

        Client c = getClientById(id);

        boolean removed = c != null;

        if(removed) SocketifyServer.clients.remove(c);

        return removed;
    }

    public Client getClientById(int id){

        for(Client c : SocketifyServer.clients) if(c.getId() == id) return c;

        return null;
    }

    private void clientUnreachable(Client client){
        fireClientDisconnectedEvent(new ClientDisconnectedEvent(this, SocketifyServer.clients.indexOf(client)));
        SocketifyServer.clients.remove(client);
    }

    private void sendCallbackTo(long packetId, int clientId){

    }

    public void sendTo(Packet packet, InetAddress host, int port) throws IOException{
        byte[] packetByteArray = Packet.toByteArray(packet);
        serverSocket.send(new DatagramPacket(packetByteArray, packetByteArray.length, host, port));
    }

    public void sendTo(Packet packet, int id) throws IOException{
        if(listening) {
            for (fr.nathsou.Tcp.Client client : SocketifyServer.clients) {
                if (client.getId() == id) {
                    byte[] packetByteArray = Packet.toByteArray(packet);
                    try {
                        serverSocket.send(new DatagramPacket(packetByteArray, packetByteArray.length, client.getAddress(), port));
                    }catch (SocketException se){
                        clientUnreachable(client);
                    }
                    break;
                }
            }
        }
    }

        public void sendToAll(Packet packet) throws IOException{
            if(listening) {
                ArrayList<Client> removeList = new ArrayList<>();
                for (Client client : SocketifyServer.clients) {
                    byte[] packetByteArray = Packet.toByteArray(packet);
                    try {
                        serverSocket.send(new DatagramPacket(packetByteArray, packetByteArray.length, client.getAddress(), port));
                    }catch (SocketException se){
                        fireClientDisconnectedEvent(new ClientDisconnectedEvent(this, SocketifyServer.clients.indexOf(client)));
                        removeList.add(client);
                    }
                }

            for(Client c : removeList) SocketifyServer.clients.remove(c);
        }
    }

    public void sendToAllExcept(Packet packet, int exceptId) throws IOException{
        if(listening) {
            for (Client client : SocketifyServer.clients) {
                if (client.getId() != exceptId) {
                    byte[] packetByteArray = Packet.toByteArray(packet);
                    try {
                        serverSocket.send(new DatagramPacket(packetByteArray, packetByteArray.length, client.getAddress(), port));
                    }catch (SocketException se){
                        fireClientDisconnectedEvent(new ClientDisconnectedEvent(this, SocketifyServer.clients.indexOf(client)));
                        SocketifyServer.clients.remove(client);
                    }
                }
            }
        }
    }

    public void sendToAllExcept(Packet packet, List<Integer> exceptIds) throws IOException{
        if(listening) {
            for (Client client : SocketifyServer.clients) {
                if (!exceptIds.contains(client.getId())) {
                    byte[] packetByteArray = Packet.toByteArray(packet);
                    try {
                        serverSocket.send(new DatagramPacket(packetByteArray, packetByteArray.length, client.getAddress(), port));
                    }catch (SocketException se){
                        fireClientDisconnectedEvent(new ClientDisconnectedEvent(this, SocketifyServer.clients.indexOf(client)));
                        SocketifyServer.clients.remove(client);
                    }
                }
            }
        }
    }

    public void sendToAllExcept(Packet packet, int[] exceptIds) throws IOException{
        if(listening) {
            for (Client client : SocketifyServer.clients) {
                boolean contains = false;
                for(int id : exceptIds) if(id == client.getId()) contains = true;
                if (!contains) {
                    byte[] packetByteArray = Packet.toByteArray(packet);
                    try {
                        serverSocket.send(new DatagramPacket(packetByteArray, packetByteArray.length, client.getAddress(), port));
                    }catch (SocketException se){
                        fireClientDisconnectedEvent(new ClientDisconnectedEvent(this, SocketifyServer.clients.indexOf(client)));
                        SocketifyServer.clients.remove(client);
                    }
                }
            }
        }
    }

    public void close(){
        interrupt();
        listening = false;
        serverSocket.close();
    }

    //ClientConnected Event

    public void addClientConnectedListener(ClientConnectedListener listener) {
        listenerList.add(ClientConnectedListener.class, listener);
    }

    public void removeClientConnectedListener(ClientConnectedListener listener) {
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

    public void removeDisclientConnectedListener(ClientDisconnectedListener listener) {
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

    //Getters & Setter

    public boolean isListening() {
        return listening;
    }

    public int getPort() {
        return port;
    }

    public InetAddress getAddress(){
        return serverSocket.getInetAddress();
    }

    public int getClientCount(){
        return SocketifyServer.clients.size();
    }

}
