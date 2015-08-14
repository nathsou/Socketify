package fr.nathsou.Packets;


import com.sun.scenario.Settings;
import fr.nathsou.Socketify.SocketifyServer;

import java.io.*;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

/**
 * Created by nathan on 07/08/15.
 */
public class Packet implements Serializable{

    private Object content;
    private int senderId;
    private short type = 0;

    public Packet(Object content, int senderId) {
        this.content = content;
        this.senderId = senderId;
    }

    //Getters & Setters

    public Object getContent() {
        return content;
    }

    public void setType(short type) {
        this.type = type;
    }

    public short getType() {
        return type;
    }

    public int getSenderId() {
        return senderId;
    }

    //Byte utils

    public static byte[] toByteArray(Packet packet) throws IOException {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        ObjectOutputStream o = new ObjectOutputStream(b);
        o.writeObject(packet);
        return b.toByteArray();
    }

    public static Packet toPacket(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream b = new ByteArrayInputStream(bytes);
        ObjectInputStream o = new ObjectInputStream(b);
        return (Packet) o.readObject();
    }

    public byte[] compress() throws IOException{
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        OutputStream out = new DeflaterOutputStream(baos);
        out.write(toByteArray(this));
        out.close();

        return baos.toByteArray();
    }

    public static Packet decompress(byte[] data) throws IOException, ClassNotFoundException{
        InputStream in = new InflaterInputStream(new ByteArrayInputStream(data));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[SocketifyServer.BUFFER_SIZE];
        int len;

        while ((len = in.read(buffer)) > 0) baos.write(buffer, 0, len);

        return Packet.toPacket(baos.toByteArray());
    }

    //toString

    public String toString(){
        return content.toString();
    }

    //equals

    public boolean equals(Packet packet){
        return senderId == packet.getSenderId() && type == packet.getType() && content.equals(packet.getContent());
    }

}
