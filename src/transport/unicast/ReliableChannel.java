package transport.unicast;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Random;

import transport.TransportPacket;
import network.NetworkInterface;
import network.NetworkListener;
import network.NetworkPacket;

public class ReliableChannel extends Thread implements NetworkListener {
	private static final int MSS = 256;
	private static final byte HOPCOUNT = 3;
	
	private InetAddress address;
	private NetworkInterface networkInterface;
	private ReliableChannelListener reliableChannelListener = null;
	private TransportProtocol protocol;
	
	private byte streamNumber;
	
	public ReliableChannel(InetAddress address, NetworkInterface networkInterface) throws IOException {
		this.address = address;
		this.networkInterface = networkInterface;
		this.protocol = new SimpleTransportProtocol(this);
		this.streamNumber = (byte) (new Random().nextInt(256) + Byte.MIN_VALUE);
		
		new Thread((SimpleTransportProtocol) this.protocol).start();
	}
	
	public synchronized void sendBytes(byte[] bytes) {
		for (int fragement = 0; fragement < bytes.length; fragement += MSS) {
			int dataLength = Math.min(MSS, bytes.length - fragement);
			byte[] data = new byte[dataLength];
			
			System.arraycopy(bytes, fragement, data, 0, dataLength);
			
			TransportPacket transportPacket = new TransportPacket(data);
			transportPacket.setStreamNumber(streamNumber);
			
			if (bytes.length > MSS) {
				if (bytes.length - fragement > MSS) {
					transportPacket.setFlags(TransportPacket.FRAGMENTED | TransportPacket.MORE_FRAGMENTS);
				} else {
					transportPacket.setFlags(TransportPacket.FRAGMENTED);
				}
			}
			
			protocol.addToQueue(transportPacket);
		}
		
		streamNumber++;
	}
	
	public void receiveBytes(byte[] bytes) {
		if (reliableChannelListener != null) {
			reliableChannelListener.onReceive(address, bytes);
		}
	}
	
	public synchronized void sendTransportPacket(TransportPacket transportPacket) {
		NetworkPacket networkPacket = new NetworkPacket(networkInterface.getLocalHost(), address, HOPCOUNT, transportPacket.getBytes());
		networkPacket.setFlags(NetworkPacket.TRANSPORT);
		
		try {
			networkInterface.send(networkPacket);
			
			System.out.println("SEND: seq=" + transportPacket.getSequenceNumber() + ", ack=" + transportPacket.getAcknowledgeNumber());
		} catch (IOException e) { 
			e.printStackTrace();
		}
	}
	
	@Override
	public synchronized void onReceive(NetworkPacket packet) {
		if (packet.isFlagSet(NetworkPacket.TRANSPORT)) {			
			if (packet.getDestinationAddresses().contains(networkInterface.getLocalHost()) && packet.getSourceAddress().equals(address) && !packet.isFlagSet(NetworkPacket.MULTICAST)) {
				TransportPacket transportPacket = TransportPacket.parseBytes(packet.getData());
				
				if (transportPacket != null) {
					System.out.println("RECV: seq=" + transportPacket.getSequenceNumber() + ", ack=" + transportPacket.getAcknowledgeNumber());
					System.out.println("DATA: " + new String(transportPacket.getData()));
					
					protocol.onReceive(transportPacket);
				}
			}
		}
 	}
	
	public void setReliableChannelListener(ReliableChannelListener reliableChannelListener) {
		this.reliableChannelListener = reliableChannelListener;
	}
	
}
