package fr.nathsou.Tcp;

import fr.nathsou.Events.PacketReceivedEvent.PacketReceivedEvent;
import fr.nathsou.Events.PacketReceivedEvent.PacketReceivedListener;
import fr.nathsou.Packets.Packet;

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

    public TcpClient(String host, int port) throws UnknownHostException{

        this.port = port;
        try{
            this.host = InetAddress.getByName(host);
        }catch (IOException ioe){
            ioe.printStackTrace();
        }

        listenerList = new EventListenerList();

    }

    public void connect() {
        if(!connected) {
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
                        while(connected){
                            try {
                                Packet packet = (Packet) in.readObject();
                                firePacketReceivedEvent(new PacketReceivedEvent(this, packet, 0)); //0 -> Server
                            }catch (ClassNotFoundException cnf) {
                                cnf.printStackTrace();
                            }catch (SocketException se){
                                //se.printStackTrace();
                                disconnect();
                            }catch (IOException ioe){
                                ioe.printStackTrace();
                            }
                        }
                    }
                }).start();

            }catch (IOException | ClassNotFoundException e){
                e.printStackTrace();
            }
        }
    }

    public void disconnect(){
        if(connected) {
            try {
                out.close();
                in.close();
                connected = false;
                connection.close();
            }catch (IOException ioe){
                ioe.printStackTrace();
            }
        }
    }

    public void send(Object obj){

        if(connected){
            try{
                //connection.setSoTimeout(2000);
                out.writeObject(new Packet(obj, id));
                out.flush();
            }catch (IOException ioe){
                ioe.printStackTrace();
            }
        }else{
            throw new Error("Client must be connected to a server in order to send a packet");
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
}
