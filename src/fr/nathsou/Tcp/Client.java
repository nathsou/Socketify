package fr.nathsou.Tcp;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by nathan on 07/08/15.
 */
public class Client {

    private int id;
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private InetAddress address;

    public Client(Socket socket, int id) throws IOException{
        this.id = id;
        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();
        in = new ObjectInputStream(socket.getInputStream());
        address = socket.getInetAddress();
    }

    public Client(InetAddress address, int id, int port) throws IOException{
        this.id = id;
        socket = new Socket(address, port);
        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();
        in = new ObjectInputStream(socket.getInputStream());
        this.address = address;
    }

    public void close(){
        try {
            out.close();
            in.close();
            //socket.close();
        }catch (IOException ioe){
            ioe.printStackTrace();
        }
    }

    //Getters & Setters


    public int getId() {
        return id;
    }

    public Socket getSocket() {
        return socket;
    }

    public ObjectInputStream getObjectInputStream() {
        return in;
    }

    public ObjectOutputStream getObjectOutputStream() {
        return out;
    }

    public InetAddress getAddress() {
        return address;
    }

    public void writeObject(Object obj) throws IOException{
        out.writeObject(obj);
        out.flush();
    }

    public Object readObject() throws ClassNotFoundException, IOException{
        return in.readObject();
    }
}
