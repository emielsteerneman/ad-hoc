package network;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

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
	
	public static class ParseException extends ExecutionException {
		private static final long serialVersionUID = 7294253795248977288L;

		public ParseException() {
			super();
		}

		public ParseException(String message) {
			super(message);
		}
		
	}
	
	public static NetworkPacket parseBytes(byte[] bytes) throws ParseException {
		NetworkPacket networkPacket = null;
		
		if (bytes.length < 8) {
			throw new NetworkPacket.ParseException("Not enough bytes");
		}
		
		byte flags = bytes[0];
		byte hopcount = bytes[1];
		byte headerSize = bytes[2];
		byte reserved = bytes[3];
		
		InetAddress sourceAddress = null;
		
		try {
			sourceAddress = InetAddress.getByAddress(Arrays.copyOfRange(bytes, 0, 4));
		} catch (UnknownHostException e) {
			throw new NetworkPacket.ParseException(e.getMessage());
		}
		
		List<InetAddress> destinationAddresses = new ArrayList<>();
		
		for (int i = 2; i < headerSize; i++) {
			if ((i * 4) + 4 >= bytes.length) {
				throw new NetworkPacket.ParseException("not enough bytes");
			}
			
			InetAddress destinationAddress = null;
			
			try {
				destinationAddress = InetAddress.getByAddress(Arrays.copyOfRange(bytes, i * 4, (i * 4) + 4));	
			} catch (UnknownHostException e) {
				throw new NetworkPacket.ParseException(e.getMessage());
			}
			
			destinationAddresses.add(destinationAddress);
		}
		
		byte[] data = Arrays.copyOfRange(bytes, headerSize, bytes.length);

		networkPacket = new NetworkPacket(sourceAddress, destinationAddresses, hopcount, data);
		networkPacket.setFlags(flags);
		networkPacket.setReserved(reserved);
		
		return networkPacket;
	}
	
}