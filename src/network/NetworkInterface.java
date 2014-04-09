package network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import network.routing.RoutingProtocol;
import network.routing.SimpleRoutingProtocol;

public class NetworkInterface extends Thread {
	private static final int BUFFER_SIZE = 512;
	private static final int TIME_OUT = 2000;
	
	private int port;
	private InetAddress group;
	
	private InetAddress localHost;
	
	//private MulticastSocket receiveSocket;
	private DatagramSocket receiveSocket;
	private DatagramSocket sendSocket;	
	
	private List<NetworkListener> networkListeners;
	private RoutingProtocol routingProtocol;
	
	private Queue<DatagramPacket> localQueue;
	
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
				
				try {
					receive(localQueue.poll());
				} catch (IOException e) { }
			}
		}
	}
	
	public NetworkInterface(InetAddress group, int port) throws IOException {
		this.group = group;
		this.port = port;
		
		this.localHost = InetAddress.getLocalHost();
		
		//this.receiveSocket = new MulticastSocket(port);
		//this.receiveSocket.joinGroup(group);
		this.receiveSocket = new DatagramSocket(port);
		this.receiveSocket.setSoTimeout(TIME_OUT);
		this.sendSocket = new DatagramSocket();
		
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
				synchronized (localQueue) {
					localQueue.add(packet);
					localQueue.notify();
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
	
	public synchronized void process(NetworkPacket networkPacket) {
		for (NetworkListener networkListener : networkListeners) {
			if (networkListener != null) {
				networkListener.onReceive(networkPacket);
			}
		}
	}
	
	public synchronized void send(NetworkPacket networkPacket) throws IOException {
		byte[] packetData = networkPacket.getBytes();

		DatagramPacket packet = new DatagramPacket(packetData, packetData.length, group, port);
		
		sendSocket.send(packet);
	}
	
	private synchronized void receive(DatagramPacket packet) throws IOException {
		NetworkPacket networkPacket = NetworkPacket.parseBytes(packet.getData());
		
		if (networkPacket != null) {
			routingProtocol.rout(networkPacket);
		}
	}
	
}