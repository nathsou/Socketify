package Socketify.Tcp;

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
    private int port;

    public Client(Socket socket, int id) throws IOException{
        this.id = id;
        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();
        in = new ObjectInputStream(socket.getInputStream());
        address = socket.getInetAddress();
        port = socket.getPort();
    }

    public Client(InetAddress address, int id, int port) throws IOException{
        this.id = id;
        this.port = port;
        this.address = address;
    }

    public void close() throws IOException{
        if(out != null) out.close();
        if(in != null) in.close();
    }

    //Getters & Setters


    public int getId() {
        return id;
    }

    public Socket getSocket() throws IOException{

        if(socket != null) socket = new Socket(address, port);

        return socket;
    }

    public int getPort() {
        return port;
    }

    public ObjectInputStream getObjectInputStream() throws IOException{

        if(socket != null) socket = new Socket(address, port);
        if(in != null) in = new ObjectInputStream(socket.getInputStream());

        return in;
    }

    public ObjectOutputStream getObjectOutputStream() throws IOException{

        if(socket != null) socket = new Socket(address, port);
        if(out != null) out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();
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
