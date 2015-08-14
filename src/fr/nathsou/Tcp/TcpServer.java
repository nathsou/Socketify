package fr.nathsou.Tcp;

import fr.nathsou.Events.ClientConnectedEvent.ClientConnectedEvent;
import fr.nathsou.Events.ClientConnectedEvent.ClientConnectedListener;
import fr.nathsou.Events.ClientDisconnectedEvent.ClientDisconnectedEvent;
import fr.nathsou.Events.ClientDisconnectedEvent.ClientDisconnectedListener;
import fr.nathsou.Events.PacketReceivedEvent.PacketReceivedEvent;
import fr.nathsou.Events.PacketReceivedEvent.PacketReceivedListener;
import fr.nathsou.Packets.Packet;
import fr.nathsou.Socketify.SocketifyServer;

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
public class TcpServer extends Thread{

    private int port;
    private ServerSocket serverSocket;
    private EventListenerList listenerList;
    private int id = 0;
    private Socket connection;

    public TcpServer(int port){

        this.port = port;
        try {
            serverSocket = new ServerSocket(port);
            //serverSocket.setSoTimeout(2000);
        }catch (IOException ioe){
            ioe.printStackTrace();
        }
        SocketifyServer.clients = new ArrayList<>();
        listenerList = new EventListenerList();
    }

    public void listen(){
        start();
    }

    @Override
    public void run() {
        super.run();

        try {

            while(true){
                connection = serverSocket.accept();
                id++;
                Client client = new Client(connection, id);
                SocketifyServer.clients.add(client);
                client.writeObject(id);
                fireClientConnectedEvent(new ClientConnectedEvent(this, id));

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        while(true){
                            try{
                                Packet packet = (Packet) client.readObject();
                                firePacketReceivedEvent(new PacketReceivedEvent(this, packet, client.getId()));
                            }catch (ClassNotFoundException cnf){
                                cnf.printStackTrace();
                            }catch (IOException ioe){
                                client.close();
                                SocketifyServer.clients.remove(client);
                                fireClientDisconnectedEvent(new ClientDisconnectedEvent(this, client.getId()));
                                break;
                            }
                        }
                    }
                }).start();

            }

        }catch (SocketException se){

        }catch (IOException ioe){
            ioe.printStackTrace();
        }
    }

    public void sendTo(Object obj, int clientId) throws IOException{
        for(Client c : SocketifyServer.clients){
            if(c.getId() == clientId){
                    c.writeObject(new Packet(obj, -1));
                break;
            }
        }
    }

    public void sendToAll(Object obj) throws IOException{
        for(Client c : SocketifyServer.clients) c.writeObject(new Packet(obj, -1));
    }

    public void sendToAllExcept(Object obj, int exceptId) throws IOException{
        for(Client c : SocketifyServer.clients) if (c.getId() != exceptId) c.writeObject(new Packet(obj, -1));
    }

    public void sendToAllExcept(Object obj, List<Integer> exceptIds) throws IOException{
        for(Client c : SocketifyServer.clients) if (!exceptIds.contains(c.getId())) c.writeObject(new Packet(obj, -1));
    }

    public void sendToAllExcept(Object obj, int[] exceptIds) throws IOException{

        for(Client c : SocketifyServer.clients){
            boolean contains = false;
            for(int id : exceptIds) if(id == c.getId()){contains = true;}
            if (!contains) c.writeObject(new Packet(obj, -1));
        }
    }


    public void close(){
        interrupt();
        try {
            if(!serverSocket.isClosed()) {
                serverSocket.close();
                id = 0;
            }
            //for(Client c : SocketifyServer.clients){if(!c.getSocket().isClosed()) c.getSocket().close();}

        }catch (IOException ioe){
            ioe.printStackTrace();
        }
    }

    //Getters && Setters

    public InetAddress getInetAddress(){
        return serverSocket.getInetAddress();
    }

    public int getPort() {
        return port;
    }

    public int clientCount(){
        return SocketifyServer.clients.size();
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

    public void removeClientDisconnectedListener(ClientDisconnectedListener listener) {
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


}
