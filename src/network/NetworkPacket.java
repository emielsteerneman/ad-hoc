package network;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class NetworkPacket {
	byte flags = 0;
	byte hopcount = 0;
	byte reserved = 0;
		
	private InetAddress sourceAddress;
	private List<InetAddress> destinationAddresses;
	
	private byte[] data;
	
	public NetworkPacket(InetAddress sourceAddress, InetAddress destinationAddress, byte hopcount, byte[] data) {
		this.sourceAddress = sourceAddress;
		this.destinationAddresses = new ArrayList<>();
		this.destinationAddresses.add(destinationAddress);
		this.hopcount = hopcount;
		this.data = data;
	}
	
	public NetworkPacket(InetAddress sourceAddress, List<InetAddress> destinationAddresses, byte hopcount, byte[] data) {
		this.sourceAddress = sourceAddress;
		this.destinationAddresses = destinationAddresses;
		this.hopcount = hopcount;
		this.data = data;
	}
	
	public boolean isMulticast() {
		return destinationAddresses.size() > 1;
	}
	
	public byte getFlags() {
		return flags;
	}
	
	public byte getReserved() {
		return reserved;
	}
	
	public byte getHopcount() {
		return hopcount;
	}
	
	public byte getHeaderSize() {
		return (byte) (2 + destinationAddresses.size());
	}
	
	public int getHeaderLength() {
		return (byte) (8 + 4 * destinationAddresses.size());
	}
	
	public InetAddress getSourceAddress() {
		return sourceAddress;
	}
	
	public List<InetAddress> getDestinationAddresses() {
		return destinationAddresses;
	}
	
	public byte[] getData() {
		return data;
	}
	
	public int getLength() {
		return data.length;
	}
	
	public void setFlags(byte flags) {
		this.flags = flags;
	}
	
	public void setHopcount(byte hopcount) {
		this.hopcount = hopcount;
	}
	
	public void decrementHopcount() {
		this.hopcount--;
	}
	
	public void setReserved(byte reserved) {
		this.reserved = reserved;
	}
	
	public void setSourceAddress(InetAddress sourceAddress) {
		this.sourceAddress = sourceAddress;
	}
	
	public void setData(byte[] data) {
		this.data = data;
	}
	
	public byte[] getBytes() {
		byte[] bytes = new byte[getHeaderLength() + data.length];
		
		bytes[0] = flags;
		bytes[1] = hopcount;
		bytes[2] = getHeaderSize();
		bytes[3] = reserved;
		
		System.arraycopy(sourceAddress.getAddress(), 0, bytes, 4, 4);
		
		for (int i = 0; i < destinationAddresses.size(); i++) {
			byte[] destinationAddress = destinationAddresses.get(i).getAddress();
			
			System.arraycopy(destinationAddress, 0, bytes, (i + 2) * 4, 4);
		}
		
		System.arraycopy(data, 0, bytes, getHeaderLength(), data.length);
		
		return bytes;
	}
	
	public static NetworkPacket parseBytes(byte[] bytes) {
		
		return null;
	}
	
	public static void main(String[] args) throws UnknownHostException {
		NetworkPacket packet = new NetworkPacket(InetAddress.getByAddress(new byte[4]), InetAddress.getByAddress(new byte[4]), (byte) 4, new byte[5]);
		
		packet.getBytes();
	}
	
}