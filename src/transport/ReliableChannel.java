package transport;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import network.NetworkInterface;
import network.NetworkListener;
import network.NetworkPacket;

public class ReliableChannel extends Thread implements NetworkListener {
	public static final int HIGH_PRIORITY = 0;
	public static final int MEDIUM_PRIORITY = 1;
	public static final int LOW_PRIORITY = 2;
	
	private static final int MSS = 10;
	private static final byte HOPCOUNT = 4;
	
	private InetAddress address;
	private NetworkInterface networkInterface;
	
	private Queue<TransportPacket> sendQueue = new LinkedBlockingQueue<>();
	
	private byte streamNumber = 0;
	
	private boolean waitingForAck = false;
	private int acknowledgeNumber = 0;
	
	private int lastAcknowledgeNumberSent = 0;
	
	private ByteBuffer stream = ByteBuffer.allocate(0);
	
	public ReliableChannel(InetAddress address, NetworkInterface networkInterface) throws IOException {
		this.address = address;
		this.networkInterface = networkInterface;
	}
	
	@Override
	public void run() {
		while (true) {
			synchronized (sendQueue) {
				while (waitingForAck) {
					try {
						sendQueue.wait();
					} catch (InterruptedException e) { }
				}
				
				if (waitingForAck) {
					TransportPacket transportPacket = sendQueue.peek();
					
					acknowledgeNumber = transportPacket.getSequenceNumber();
					waitingForAck = true;
					
					NetworkPacket networkPacket = new NetworkPacket(networkInterface.getLocalHost(), address, HOPCOUNT, transportPacket.getBytes());
					networkPacket.setFlags(NetworkPacket.TRANSPORT_FLAG);
					
					try {
						networkInterface.send(networkPacket);
					} catch (IOException e) { }	
				}	
			}
		}
	}
	
	public void send(byte[] bytes, int priority) {
		for (int position = 0; position < bytes.length; position += MSS) {
			int dataLength = Math.min(MSS, bytes.length - position);
			byte[] data = new byte[dataLength];
			
			System.arraycopy(bytes, position, data, 0, dataLength);
			
			TransportPacket transportPacket = new TransportPacket(data);
			transportPacket.setSequenceNumber(position);
			transportPacket.setStreamNumber(streamNumber);
			
			if (bytes.length > MSS) {
				if (bytes.length - position > MSS) {
					transportPacket.setFlags(TransportPacket.FRAGMENTED | TransportPacket.MORE_FRAGMENTS_FLAG);
				} else {
					transportPacket.setFlags(TransportPacket.FRAGMENTED);
				}
			}
			
			sendQueue.add(transportPacket);
		}
		
		streamNumber++;
	}
	
	@Override
	public void onReceive(NetworkPacket packet) {
		if (!packet.isFlagSet(NetworkPacket.TRANSPORT_FLAG)) {
			return;
		}
		
		if (packet.getDestinationAddresses().contains(address)) {
			TransportPacket transportPacket = TransportPacket.parseBytes(packet.getData());
			
			if (transportPacket.isFlagSet(TransportPacket.ACK_FLAG)) {
				if (transportPacket.getAcknowledgeNumber() == acknowledgeNumber && waitingForAck) {
					synchronized (sendQueue) {
						sendQueue.poll();
						sendQueue.notify();
						waitingForAck = false;	
					}
				}
			} else {
				if (transportPacket.getSequenceNumber() == lastAcknowledgeNumberSent) {
					TransportPacket ackPacket = new TransportPacket(new byte[0]);
					
					ackPacket.setAcknowledgeNumber(transportPacket.getSequenceNumber());
				}
				
				// continue over here dont yet must look at TCP.
			}
		}
 	}
	
	public static void main(String[] args) {
		int a = 0b0000000000000000000000000000001;
		
		System.out.println(a);
	}
}
