package p2pchatroom.core;

import p2pchatroom.core.events.DiscoveryEventListener;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;

public class DiscoveryListenerThread extends Thread {
    private InetAddress address;
    private int port;
    private MulticastSocket socket;
    private String programName;
    private ArrayList<DiscoveryEventListener> eventListeners;
    
    public DiscoveryListenerThread(InetAddress address, int port, String programName) throws IOException {
        this.address = address;
        this.port = port;
        this.programName = programName;
        
        socket = new MulticastSocket(port);
        socket.joinGroup(address);
        
        eventListeners = new ArrayList<DiscoveryEventListener>();
    }

    public DiscoveryListenerThread(String host, int port, String programName) throws IOException {
        this(InetAddress.getByName(host), port, programName);
    }
    
    public void addEventListener(DiscoveryEventListener eventListener) {
        eventListeners.add(eventListener);
    }

    @Override
    public void interrupt() {
        super.interrupt();
        socket.close();
    }

    public void run() {
        byte[] buffer = programName.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

        while (!isInterrupted()) {
            try {
                socket.receive(packet);
                System.out.println("Received");
                clientDiscovered(packet.getAddress());
            } catch (IOException e) {
                if (!isInterrupted()) {
                    ioError(e);
                } else {
                    System.out.println("Interrupted");
                }
            }
        }
    }
    
    private void clientDiscovered(InetAddress address) {
        for (DiscoveryEventListener eventListener : eventListeners) {
            eventListener.onClientDiscovered(address);
        }
    }

    private void ioError(IOException exception) {
        for (DiscoveryEventListener eventListener : eventListeners) {
            eventListener.onIOError(exception);
        }
    }
    
    public static void main(String[] args) {
        try {
            DiscoveryListenerThread thread = new DiscoveryListenerThread("239.255.255.255", 1667, "P2PChatRoom");
            System.out.println("Discovery listener created.");

            DiscoveryBroadcaster broadcaster = new DiscoveryBroadcaster();

            thread.start();
            System.out.println("Discovery listener started.");

            Thread.sleep(1000);

            thread.interrupt();
            System.out.println("Discovery listener interrupted.");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
