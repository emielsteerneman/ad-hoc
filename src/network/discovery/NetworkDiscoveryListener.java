package network.discovery;

import network.NetworkDevice;

public interface NetworkDiscoveryListener {
	public void onDeviceDiscovery(NetworkDevice networkDevice);
	public void onDeviceTimeout(NetworkDevice networkDevice);
}