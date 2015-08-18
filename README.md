## Usage

Socketify enables you to create client/server couples using TCP and/or UDP protocols.

The library provides the following classes:
* TcpClient and TcpServer for the TCP protocol.
* UdpClient and UdpServer for the UDP protocol.
* SocketifyClient and SocketifyServer for both protocols.

All of those Client/Server couples share the same methods, so it's as easy to create a TCP and UDP couple as just a TCP one !

```java

//The SocketifyServer class accepts an optional 3rd argument representing the UDP port.
SocketifyServer server = new SocketifyServer("localhost", 1621, 1789); //String address, int tcpPort, int udpPort (optional)
server.listen(); //Starts the server

SocketifyClient client = new SocketifyClient(1621, 1789); //int tcpPort, int udpPort (optional)

client.connect();

client.send("Hoy !", ProtocolType.TCP);

server.sendToAll("How are you ?", ProtocolType.UDP);

client.disconnect();

server.close();

```

##Event handling

Server classes fire the following events:
* PacketReceivedEvent
* PacketSentEvent

Client classes fire the following events:
* ClientConnectedEvent
* ClientDisconnectedEvent
* PacketReceivedEvent
* PacketSentEvent

```java

server.addPacketReceivedListener(new PacketReceivedListener() {
   @Override
   public void PacketReceived(PacketReceivedEvent e) {
      System.out.println("Server received (" + e.getProtocol() + "): " + e.getPacket() + " from " + e.getSenderId())
   }
});

//Using lambda expressions:

server.addPacketReceivedListener(e -> 
  System.out.println("Server received (" + e.getProtocol() + "): " + e.getPacket() + " from " + e.getSenderId())
);

```
