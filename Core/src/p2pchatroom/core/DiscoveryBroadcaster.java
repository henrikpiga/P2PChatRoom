package p2pchatroom.core;

import java.io.IOException;
import java.net.*;

public class DiscoveryBroadcaster {
    private String host;
    private int port;

    public DiscoveryBroadcaster(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void send(String message) throws IOException, UnknownHostException {
        byte[] buffer = message.getBytes();
        InetAddress group = InetAddress.getByName(host);
        DatagramSocket socket = new DatagramSocket(port);
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, port);
        socket.send(packet);
    }

    public static void main(String[] args) throws IOException, SocketException {
        new Thread() {
            public void run() {
                DiscoveryListener listener = new DiscoveryListener("239.255.255.255", 1666);
                listener.listen();
            }
        }.start();
        new Thread() {
            public void run() {
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                DiscoveryBroadcaster broadcaster = new DiscoveryBroadcaster("239.255.255.255", 1666);
                try {
                    broadcaster.send("P2PChatRoom");
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }.start();
    }
}
