package transport;



public class TransportPacket {
	public static final byte ACK					= 0b00000001;
	public static final byte MULTICAST 				= 0b00000010;
	public static final byte FRAGMENTED 			= 0b00000100;
	public static final byte MORE_FRAGMENTS 		= 0b00001000;
	
	private static final int HEADER_LENGTH = 12;
	
	private int sequenceNumber;
	private int acknowledgeNumber;
	
	private byte flags;
	private byte streamNumber;
	private byte reserved;
	private byte padding;

	private byte[] data;
	
	public TransportPacket(byte[] data) {
		this (0, 0, (byte) 0, (byte) 0, data);
	}
	
	public TransportPacket(int sequenceNumber, int acknowledgeNumber, byte flags, byte streamNumber, byte[] data) {
		this.sequenceNumber = sequenceNumber;
		this.acknowledgeNumber = acknowledgeNumber;
		this.flags = flags;
		this.streamNumber = streamNumber;
		this.reserved = 0;
		this.padding = 0;
		this.data = data;
	}
	
	public int getSequenceNumber() {
		return sequenceNumber;
	}
	
	public int getAcknowledgeNumber() {
		return acknowledgeNumber;
	}
	
	public byte getFlags() {
		return flags;
	}
	
	public boolean isFlagSet(byte flag) {
		return (flags & flag) == flag;
	}
	
	public byte getStreamNumber() {
		return streamNumber;
	}
	
	public byte getReserved() {
		return reserved;
	}
	
	public byte[] getData() {
		return data;
	}
	
	public int getLength() {
		return data.length;
	}
	
	public void setSequenceNumber(int sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}
	
	public void setAcknowledgeNumber(int acknowledgeNumnber) {
		this.acknowledgeNumber = acknowledgeNumnber;
	}
	
	public void setFlags(int flags) {
		this.flags = (byte) flags;
	}
	
	public void setStreamNumber(byte streamNumber) {
		this.streamNumber = streamNumber;
	}
	
	public void setReserved(byte reserved) {
		this.reserved = reserved;
	}
	
	public void setData(byte[] data) {
		this.data = data;
	}
	
	public byte[] getBytes() {
		byte[] bytes = new byte[HEADER_LENGTH + data.length];
		
		bytes[0] = (byte) (sequenceNumber >>> 24);
		bytes[1] = (byte) (sequenceNumber >>> 16);
		bytes[2] = (byte) (sequenceNumber >>>  8);
		bytes[3] = (byte) (sequenceNumber >>>  0);
		
		bytes[4] = (byte) (acknowledgeNumber >>> 24);
		bytes[5] = (byte) (acknowledgeNumber >>> 16);
		bytes[6] = (byte) (acknowledgeNumber >>>  8);
		bytes[7] = (byte) (acknowledgeNumber >>>  0);	
		
		bytes[8] = flags;
		bytes[9] = streamNumber;
		bytes[10] = reserved;
		bytes[11] = padding;
		
		System.arraycopy(data, 0, bytes, HEADER_LENGTH, data.length);
		
		return bytes;
	}

	public static TransportPacket parseBytes(byte[] bytes) {
		if (bytes.length < TransportPacket.HEADER_LENGTH) {
			return null;
		}
		
		int sequenceNumber = (bytes[0]         << 24) |
			                ((bytes[1] & 0xFF) << 16) |
			                ((bytes[2] & 0xFF) <<  8) |
			                ((bytes[3] & 0xFF) <<  0);
		
		int acknowledgeNumber = (bytes[4]         << 24) |
		                       ((bytes[5] & 0xFF) << 16) |
		                       ((bytes[6] & 0xFF) <<  8) |
		                       ((bytes[7] & 0xFF) <<  0);
		
		byte flags = bytes[8];
		byte streamNumber = bytes[9];
		byte reserved = bytes[10];
		
		byte[] data = new byte[bytes.length - TransportPacket.HEADER_LENGTH];
		
		System.arraycopy(bytes, 12, data, 0, data.length);
		
		TransportPacket transportPacket = new TransportPacket(sequenceNumber, acknowledgeNumber, flags, streamNumber, data);
		transportPacket.setReserved(reserved);
		
		return transportPacket;
	}
	
	public static int compareSequenceNumbers(int sequenceNumber1, int sequenceNumber2) {
		long i1 = ((long) sequenceNumber1) & 0xFFFFFFFF;
		long i2 = ((long) sequenceNumber2) & 0xFFFFFFFF;
		int serialBits = 32;
		
		if ((i1 < i2 && i2 - i1 < (Math.pow(2, (serialBits - 1)))) ||
		    (i1 > i2 && i1 - i2 > (Math.pow(2, (serialBits - 1))))) {
			return -1;
		} else if (i1 == i2) {
			return 0;
		} else {
			return 1;
		}
	}
	
}