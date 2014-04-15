package transport.unicast;

import transport.TransportPacket;

public interface TransportProtocol {
	public void onReceive(TransportPacket transportPacket);
	public void addToQueue(TransportPacket transportPacket);
}