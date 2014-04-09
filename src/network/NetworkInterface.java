package network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.List;

import routing.RoutingProtocol;
import routing.SimpleRoutingProtocol;

public class NetworkInterface extends Thread {
	private static final int PORT = 4446;
	private static final String GROUP = "226.2.2.2";
	
	private MulticastSocket receiveSocket = null;
	private DatagramSocket sendSocket = null;
	private InetAddress localHost = null;
	private InetAddress group = null;
	
	private static final int BUFFER_SIZE = 256;
	
	private List<NetworkListener> networkListeners = null;
	private RoutingProtocol routingProtocol = null;
	
	public NetworkInterface() throws IOException {
		receiveSocket = new MulticastSocket(PORT);
		
		localHost = InetAddress.getLocalHost();
		group = InetAddress.getByName(GROUP);
		
		receiveSocket.joinGroup(group);
		sendSocket = new DatagramSocket();
		
		routingProtocol = new SimpleRoutingProtocol();
	}
	
	@Override
	public void run() {
		boolean running = true;
		
		while (running) {
			DatagramPacket packet = new DatagramPacket(new byte[BUFFER_SIZE], BUFFER_SIZE);
			
			try {
				receiveSocket.receive(packet);
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

		DatagramPacket packet = new DatagramPacket(packetData, packetData.length, group, PORT);
		
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