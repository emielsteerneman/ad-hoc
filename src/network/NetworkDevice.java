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
		return "[" + address.toString() + ": " + identifier + "]";
	}

	@Override
	public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj == this)
            return true;
        if (!(obj instanceof NetworkDevice))
            return false;
        
        NetworkDevice networkDevice = (NetworkDevice) obj;
		
		return address.equals(networkDevice.getAddress());
	}
	
}
