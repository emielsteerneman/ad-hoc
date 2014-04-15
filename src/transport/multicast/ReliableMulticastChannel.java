package transport.multicast;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Random;

import transport.TransportPacket;
import transport.unicast.ReliableChannelListener;
import transport.unicast.TransportProtocol;
import network.NetworkInterface;
import network.NetworkListener;
import network.NetworkPacket;

public class ReliableMulticastChannel extends Thread implements NetworkListener {
	private static final int MSS = 1; //256;
	private static final byte HOPCOUNT = 4;	
	
	private HashMap<InetAddress, Boolean> devices;
	
	private NetworkInterface networkInterface;
	private ReliableMulticastChannelListener reliableMulticastChannelListener = null;
	private TransportProtocol protocol;
	
	private byte streamNumber;
	
	public ReliableMulticastChannel(NetworkInterface networkInterface) {
		this.networkInterface = networkInterface;
		this.streamNumber = (byte) (new Random().nextInt(256) + Byte.MIN_VALUE);
	}
	
	@Override
	public void onReceive(NetworkPacket packet) {
		
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
	
	public void addDevice(InetAddress device) {
		devices.put(device, false);
	}
	
	public void removeDevice(InetAddress device) {
		devices.remove(device);
	}
	
	public void setReliableMulticastChannelListener(ReliableMulticastChannelListener reliableMulticastChannelListener) {
		this.reliableMulticastChannelListener = reliableMulticastChannelListener;
	}
	
	//fix this shit bro
}