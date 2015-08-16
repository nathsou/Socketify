package Socketify.Socketify;

import Socketify.Tcp.TcpClient;
import Socketify.Udp.UdpClient;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Created by nathan on 14/08/15.
 */
public class SocketifyClient {

    private int id = SocketifyServer.clients != null ? SocketifyServer.clients.size() : 0;

}
