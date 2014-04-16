package network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

import network.routing.RoutingProtocol;
import network.routing.SimpleRoutingProtocol;

public class NetworkInterface extends Thread {
	public static final byte HOPCOUNT = 4;
	
	private static final int BUFFER_SIZE = 512;
	private static final int TIME_OUT = 2000;
	
	private int port;
	private InetAddress group;
	
	//private MulticastSocket receiveSocket;
	private DatagramSocket receiveSocket;
	private DatagramSocket sendSocket;	
	
	private InetAddress localHost;
	
	private List<NetworkListener> networkListeners;
	private RoutingProtocol routingProtocol;
	
	private Queue<NetworkPacket> localQueue;
	
	private class QueueProcessor implements Runnable {
		@Override
		public void run() {
			while (true) {
				while (localQueue.isEmpty()) {
					synchronized (localQueue) {
						try {
							localQueue.wait();
						} catch (InterruptedException e) { }	
					}
				}
				
				if (!localQueue.isEmpty()) {
					synchronized (localQueue) {
						try {
							routingProtocol.rout(localQueue.poll());
						} catch (IOException e) { }		
					}
				}
			}
		}
	}
	
	public NetworkInterface(InetAddress group, int port, InetAddress localHost) throws IOException {
		this.group = group;
		this.port = port;
		
		//this.receiveSocket = new MulticastSocket(port);
		//this.receiveSocket.joinGroup(group);
		this.receiveSocket = new DatagramSocket(port);
		this.receiveSocket.setSoTimeout(TIME_OUT);
		this.sendSocket = new DatagramSocket();
		
		this.localHost = localHost;
		
		this.networkListeners = new ArrayList<>();
		this.routingProtocol = new SimpleRoutingProtocol(this);
		
		this.localQueue = new LinkedBlockingQueue<>();
		
		new Thread(new QueueProcessor()).start();
	}
	
	@Override
	public void run() {
		while (true) {
			DatagramPacket packet = new DatagramPacket(new byte[BUFFER_SIZE], BUFFER_SIZE);
			
			try {
				receiveSocket.receive(packet);
			} catch (IOException e) {
				packet = null;
			}
			
			if (packet != null) {
			
				NetworkPacket networkPacket = NetworkPacket.parseBytes(Arrays.copyOfRange(packet.getData(), 0, packet.getLength()));
				
				if (networkPacket != null) {
					if (new Random().nextInt(10) == 0)
						continue;
					
					synchronized (localQueue) {
						localQueue.add(networkPacket);
						localQueue.notify();
					}
				}
			}
		}
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
		List<NetworkListener> copy = new ArrayList<>(networkListeners);
		
		for (NetworkListener networkListener : copy) {
			if (networkListener != null) {
				networkListener.onReceive(networkPacket);
			}
		}
		
		copy.clear();
	}
	
	public void send(NetworkPacket networkPacket) throws IOException {
		byte[] packetData = networkPacket.getBytes();
		
		DatagramPacket packet = new DatagramPacket(packetData, packetData.length, group, port);
		
		sendSocket.send(packet);
	}
	
}