package fr.nathsou;

import fr.nathsou.Events.ClientConnectedEvent.ClientConnectedEvent;
import fr.nathsou.Events.ClientConnectedEvent.ClientConnectedListener;
import fr.nathsou.Tcp.TcpClient;
import fr.nathsou.Tcp.TcpServer;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws Exception{

        TcpServer server = new TcpServer(1621);
        server.addPacketReceivedListener(e -> System.out.println("Server received: " + e.getPacket().getContent()));
        server.addClientConnectedListener(new ClientConnectedListener() {
            @Override
            public void ClientConnected(ClientConnectedEvent ev) {
                try {
                    server.sendTo("Welcome to this server !", ev.getId());
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        });
        server.listen();

        TcpClient client = new TcpClient("localhost", 1621);
        client.addPacketReceivedListener(e -> System.out.println("Client received: " + e.getPacket()));
        client.connect();

        client.send("Yolo !");

        Thread.sleep(100);

        client.disconnect();

        server.close();

    }
}
