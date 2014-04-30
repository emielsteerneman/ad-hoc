package network.discovery;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;

import network.NetworkDevice;
import network.NetworkInterface;
import network.NetworkListener;
import network.NetworkPacket;

public class NetworkDiscovery implements NetworkListener {
	private static final int DELAY = 500;
	private static final int TIMEOUT = 10;
	
	private NetworkInterface networkInterface;
	private String identifier;
	
	private HashMap<NetworkDevice, Integer> devices;
	
	private NetworkDiscoveryListener networkDiscoveryListener = null;
	
	private Timer timer;
	
	private class BroadcastTask extends TimerTask {

		@Override
		public synchronized void run() {
			NetworkPacket networkPacket = new NetworkPacket(networkInterface.getLocalHost(), new ArrayList<InetAddress>(), NetworkInterface.HOPCOUNT, identifier.getBytes());
			networkPacket.setFlags(NetworkPacket.ARP);
			
			HashSet<NetworkDevice> remove = new HashSet<NetworkDevice>();
			for (NetworkDevice device : devices.keySet()) {
				int TTL = devices.get(device) - 1;
				
				if (TTL > 0) {
					devices.put(device, TTL);
				} else {
					remove.add(device);
					
					if (networkDiscoveryListener != null) {
						networkDiscoveryListener.onDeviceTimeout(device);
					}
				}
			}
			for(NetworkDevice dev : remove){
				devices.remove(dev);
			}
			
			try {
				networkInterface.send(networkPacket);
			} catch (IOException e) { }
		}
		
	}
	
	public NetworkDiscovery(NetworkInterface networkInterface, String identifier) {
		this.networkInterface = networkInterface;
		this.identifier = identifier;
		this.devices = new HashMap<>();
		this.timer = new Timer();
		this.timer.scheduleAtFixedRate(new BroadcastTask(), 0L, DELAY);
	}
	
	@Override
	public synchronized void onReceive(NetworkPacket packet) {
		if (!packet.isFlagSet(NetworkPacket.ARP)) {
			return;
		}
		
		NetworkDevice networkDevice = new NetworkDevice(packet.getSourceAddress(), new String(packet.getData()));
		
		if (devices.containsKey(networkDevice)) {
			devices.put(networkDevice, NetworkDiscovery.TIMEOUT);
		} else {
			devices.put(networkDevice, NetworkDiscovery.TIMEOUT);
			
			if (networkDiscoveryListener != null) {
				networkDiscoveryListener.onDeviceDiscovery(networkDevice);
			}
		}
	}
	
	public NetworkDevice getNetworkDeviceByInetAddress(InetAddress address) {
		for (NetworkDevice device : devices.keySet()) {
			if (device.getAddress().equals(address)) {
				return device;
			}
		}
		
		return null;
	}
	
	public void setNetworkDiscoveryListener(NetworkDiscoveryListener networkDiscoveryListener) {
		this.networkDiscoveryListener = networkDiscoveryListener;
	}
	
}
