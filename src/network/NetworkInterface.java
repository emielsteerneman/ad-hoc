package network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import network.routing.RoutingProtocol;
import network.routing.SimpleRoutingProtocol;

public class NetworkInterface extends Thread {
	private static final int BUFFER_SIZE = 512;
	
	private int port;
	private InetAddress group;
	
	private InetAddress localHost;
	
	private MulticastSocket receiveSocket;
	private DatagramSocket sendSocket;	
	
	private List<NetworkListener> networkListeners;
	private RoutingProtocol routingProtocol;
	
	private boolean running = false;
	
	public NetworkInterface(InetAddress group, int port) throws IOException {
		this.group = group;
		this.port = port;
		
		this.localHost = InetAddress.getLocalHost();
		
		this.receiveSocket = new MulticastSocket(port);
		this.receiveSocket.joinGroup(group);
		this.sendSocket = new DatagramSocket();
		
		this.networkListeners = new ArrayList<>();
		this.routingProtocol = new SimpleRoutingProtocol(this);
	}
	
	@Override
	public void run() {
		running = true;
		
		while (running) {
			DatagramPacket packet = new DatagramPacket(new byte[BUFFER_SIZE], BUFFER_SIZE);
			
			try {
				receiveSocket.receive(packet);
				receiveSocket.setSoTimeout(1000);
			} catch (SocketException e) {
				
			} catch (IOException e) {
				running = false;
			}
			
			if (packet != null) {
				try {
					receive(packet);
				} catch (IOException e){ }
			}
		}
		
		receiveSocket.close();
	}
	
	public InetAddress getLocalHost() {
		return localHost;
	}
	
	public void addNetworkListener(NetworkListener networkListener) {
		this.networkListeners.add(networkListener);
	}
	
	public void removeNetworkListener(NetworkListener networkListener) {
		this.networkListeners.remove(networkListener);
	}
	
	public void process(NetworkPacket networkPacket) {
		for (NetworkListener networkListener : networkListeners) {
			if (networkListener != null) {
				networkListener.onReceive(networkPacket);
			}	
		}
	}
	
	public void send(NetworkPacket networkPacket) throws IOException {
		byte[] packetData = networkPacket.getBytes();

		DatagramPacket packet = new DatagramPacket(packetData, packetData.length, group, port);
		
		sendSocket.send(packet);
	}
	
	private void receive(DatagramPacket packet) throws IOException {
		NetworkPacket networkPacket = null;
		
		try {
			networkPacket = NetworkPacket.parseBytes(packet.getData());
		} catch (NetworkPacket.ParseException e) { }
		
		if (networkPacket != null) {
			routingProtocol.rout(networkPacket);
		}
	}
	
}