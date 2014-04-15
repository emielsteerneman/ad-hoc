package network;

import java.net.InetAddress;

public class NetworkDevice {
	private InetAddress address;
	private String identifier;
	
	public NetworkDevice(InetAddress address, String identifier) {
		this.address = address;
		this.identifier = identifier;
	}
	
	public InetAddress getAddress() {
		return address;
	}
	
	public String getIdentifier() {
		return identifier;
	}
	
	@Override
	public String toString() {
		return identifier;
	}
	
}
