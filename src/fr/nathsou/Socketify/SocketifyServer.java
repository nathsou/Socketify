package fr.nathsou.Socketify;

import fr.nathsou.Packets.Packet;
import fr.nathsou.Tcp.Client;
import fr.nathsou.Tcp.TcpServer;
import fr.nathsou.Udp.UdpServer;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Nathan on 12/08/2015.
 */
public class SocketifyServer {

    public static final int BUFFER_SIZE = 65536;
    public static ArrayList<Client> clients = new ArrayList<>();

    private TcpServer tcpServer;
    private UdpServer udpServer;

    public SocketifyServer(int tcpPort, int udpPort){
        tcpServer = new TcpServer(tcpPort);
        udpServer = new UdpServer(udpPort);
    }

    public void sendToAll(Object obj, Type type) throws IOException{
        switch (type){
            case TCP:
                tcpServer.sendToAll(obj);
                break;
            case UDP:
                udpServer.sendToAll(new Packet(obj, -1));
                break;
            default:
                break;
        }
    }
}
