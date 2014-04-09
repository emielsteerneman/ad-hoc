package network;

import java.net.InetAddress;
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
	//Fixed: filling bytes without errors, getHeaderLength()
	
	public byte[] getBytes() {
		byte[] bytes = new byte[getHeaderLength() + data.length];
		
		bytes[0] = flags;
		bytes[1] = hopcount;
		bytes[2] = getHeaderSize();
		bytes[3] = reserved;
//		for(int i=0; i<8;i++)
//		System.arraycopy(src, srcPos, dest, destPos, length)
		System.arraycopy(sourceAddress.toString().getBytes(), 0, bytes, 4, 8);
	
		for (int i = 0; i < destinationAddresses.size(); i++) {
			byte[] destinationAddress = destinationAddresses.get(i).getAddress();
//			System.out.print("POS: ");
			for(int a=0; a<destinationAddress.length;a++){
				int pos = 8+a+a*i;
//				System.out.print(pos+", ");
				bytes[pos] = destinationAddress[a];
			}
//			System.out.println();
//			System.out.println(bytes.length+", "+destinationAddress.length+":: POS:"+(-1+(i+2)*4)+"... Len: "+(-1+((i+2)*4)+4));
//			System.arraycopy(destinationAddress, 0, bytes, -1+(i + 2) * 4, -1+((i + 2) * 4) + 4);
		}

		System.arraycopy(data, 0, bytes, getHeaderLength(), data.length);
		for(int i=0; i<bytes.length; i++){
//			System.out.print(bytes[i]+" ");
		}		
//		System.out.println("");

		return bytes;
	}
	
}