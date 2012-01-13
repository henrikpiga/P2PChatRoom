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
    private ArrayList<InetAddress> knownAddresses;
    private ArrayList<DiscoveryEventListener> eventListeners;
    
    public DiscoveryListenerThread(InetAddress address, int port, String programName) throws IOException {
        this.address = address;
        this.port = port;
        this.programName = programName;
        
        socket = new MulticastSocket(port);
        socket.joinGroup(address);

        knownAddresses = new ArrayList<InetAddress>();
        eventListeners = new ArrayList<DiscoveryEventListener>();
        
        knownAddresses.add(InetAddress.getLocalHost());
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
                InetAddress packetAddress = packet.getAddress();
                for (InetAddress knownAddress : knownAddresses) {
                    if (knownAddress.equals(packetAddress)) {
                        continue;
                    }
                }
                clientDiscovered(packetAddress);
                knownAddresses.add(packetAddress);
            } catch (IOException e) {
                if (!isInterrupted()) {
                    ioError(e);
                } else {
                    return;
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
}
