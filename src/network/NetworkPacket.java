package network;

import java.net.InetAddress;

public class NetworkPacket {
	public static final int HEADER_SIZE = 12;
	
	private InetAddress sourceAddress;
	private InetAddress destinationAddress;
	private int hopcount;
	private byte[] data;
	
	public NetworkPacket(InetAddress sourceAddress, InetAddress destinationAddress, int hopcount, byte[] data) {
		this.sourceAddress = sourceAddress;
		this.destinationAddress = destinationAddress;
		this.hopcount = hopcount;
		this.data = data;
	}
	
	public InetAddress getSourceAddress() {
		return sourceAddress;
	}
	
	public InetAddress getDestinationAddress() {
		return destinationAddress;
	}
	
	public int getHopcount() {
		return hopcount;
	}
	
	public byte[] getData() {
		return data;
	}
	
	public int getLength() {
		return data.length;
	}
	
	public void setSourceAddress(InetAddress sourceAddress) {
		this.sourceAddress = sourceAddress;
	}
	
	public void setDestinationAddress(InetAddress destinationAddress) {
		this.destinationAddress = destinationAddress;
	}
	
	public void setHopcount(int hopcount) {
		this.hopcount = hopcount;
	}
	
	public void setData(byte[] data) {
		this.data = data;
	}
	
}
