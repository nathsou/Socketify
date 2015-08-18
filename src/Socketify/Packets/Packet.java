package Socketify.Packets;


import Socketify.Socketify.SocketifyServer;

import java.io.*;
import java.util.zip.*;

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

    public Packet(Object content, int senderId, short type){
        this.content = content;
        this.senderId = senderId;
        this.type = type;
    }

    public Packet(Object content, int senderId, int type){
        this.content = content;
        this.senderId = senderId;
        this.type = (short) type;
    }

    //Getters & Setters

    public Object getContent() {
        return content;
    }

    public void setType(int type) {
        this.type = (short) type;
    }

    public void setType(short type){
        this.type = type;
    }

    public void setContent(Object content) {
        this.content = content;
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

    public byte[] compress() throws IOException{ //Compresses the packet

        Deflater deflater = new Deflater();
        byte[] data = Packet.toByteArray(this);
        deflater.setInput(data);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        deflater.finish();
        byte[] buffer = new byte[SocketifyServer.BUFFER_SIZE];
        while (!deflater.finished()) {
            int count = deflater.deflate(buffer);
            outputStream.write(buffer, 0, count);
        }
        outputStream.close();
        return outputStream.toByteArray();
    }

    public static Packet decompress(byte[] data) throws IOException, DataFormatException, ClassNotFoundException {
        Inflater inflater = new Inflater();
        inflater.setInput(data);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        byte[] buffer = new byte[SocketifyServer.BUFFER_SIZE];
        while (!inflater.finished()) {
            int count = inflater.inflate(buffer);
            outputStream.write(buffer, 0, count);
        }
        outputStream.close();

        return Packet.toPacket(outputStream.toByteArray());
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
