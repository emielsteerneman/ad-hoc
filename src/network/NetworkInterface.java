package network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import network.routing.RoutingProtocol;

public class NetworkInterface extends Thread {
	private static final int PORT = 4446;
	private static final String GROUP = "226.2.2.2";
	
	private MulticastSocket receiveSocket = null;
	private DatagramSocket sendSocket = null;
	private InetAddress localHost = null;
	private InetAddress group = null;
	
	private static final int BUFFER_SIZE = 256;
	
	private NetworkListener networkListener = null;
	private RoutingProtocol routingProtocol = null;
	
	public NetworkInterface() throws IOException {
		receiveSocket = new MulticastSocket(PORT);
		
		localHost = InetAddress.getLocalHost();
		group = InetAddress.getByName(GROUP);
		
		receiveSocket.joinGroup(group);
		sendSocket = new DatagramSocket();
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
	
	public void setListener(NetworkListener listener) {
		this.networkListener = listener;
	}
	
	public void setRoutingProtocol(RoutingProtocol routingProtocol) {
		this.routingProtocol = routingProtocol;
	}
	
	
	public void process(NetworkPacket networkPacket) {
		if (networkListener != null) {
			networkListener.onReceive(networkPacket);
		}
	}
	
	public void send(NetworkPacket networkPacket) throws IOException {
		byte[] packetData = networkPacket.getBytes();
		
		DatagramPacket packet = new DatagramPacket(packetData, packetData.length, group, PORT);
		
		sendSocket.send(packet);
	}
	
	private void receive(DatagramPacket packet) throws IOException {
		byte[] packetData = packet.getData();
		
		byte flags = packetData[0];
		byte hopcount = packetData[1];
		byte headerSize = packetData[2];
		byte reserved = packetData[3];
		
		InetAddress sourceAddress = InetAddress.getByAddress(Arrays.copyOfRange(packetData, 0, 4));
		
		List<InetAddress> destinationAddresses = new ArrayList<>();
		
		for (int i = 2; i < headerSize; i++) {
			destinationAddresses.add(InetAddress.getByAddress(Arrays.copyOfRange(packetData, i * 4, (i * 4) + 4)));
		}
		
		byte[] data = Arrays.copyOfRange(packetData, headerSize, packet.getLength());

		NetworkPacket networkPacket = new NetworkPacket(sourceAddress, destinationAddresses, hopcount, data);
		
		networkPacket.setFlags(flags);
		networkPacket.setReserved(reserved);
		
		routingProtocol.rout(networkPacket, this);
	}
	
}