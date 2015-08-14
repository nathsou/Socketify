package fr.nathsou.Socketify;

import fr.nathsou.Tcp.TcpClient;
import fr.nathsou.Udp.UdpClient;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Created by nathan on 14/08/15.
 */
public class SocketifyClient {

    private int id = SocketifyServer.clients != null ? SocketifyServer.clients.size() : 0;
    private TcpClient tcpClient;
    private UdpClient udpClient;
    private InetAddress host;

    public SocketifyClient(String host, int tcpPort, int udpPort) throws IOException, ClassNotFoundException{
        tcpClient = new TcpClient(host, tcpPort);
        udpClient = new UdpClient(host, udpPort);
    }

    public SocketifyClient(String host, int tcpPort) throws IOException{
        tcpClient = new TcpClient(host, tcpPort);
    }

    public void connect(){

        tcpClient.connect();

        if(udpClient != null)
            udpClient.connect();
    }
}
