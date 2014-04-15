package application;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;

import main.ChatApp;
import network.NetworkInterface;
import network.discovery.NetworkDiscovery;
import network.discovery.NetworkDiscoveryListener;
import transport.multicast.ReliableMulticastChannel;
import transport.multicast.ReliableMulticastChannelListener;
import transport.unicast.ReliableChannel;
import transport.unicast.ReliableChannelListener;


public class Main implements ReliableChannelListener, ReliableMulticastChannelListener, NetworkDiscoveryListener {
	private HashMap<InetAddress, ReliableChannel> channels;
	private HashMap<InetAddress, String> identifiers;
	
	private ReliableMulticastChannel multicastChannel;
	
	private NetworkInterface networkInterface;
	private NetworkDiscovery networkDiscovery;
	
	private InetAddress group;
	private int port = 55555;
	
	private ChatApp chatapp;
	
	@Override
	public void onDeviceDiscovery(InetAddress device, String identifier) {
		ReliableChannel channel = null;
		
		try {
			 channel = new ReliableChannel(device, networkInterface);
		} catch (IOException e) { }
		
		if (channel != null) {
			channel.setReliableChannelListener(this);
			channel.start();
			
			channels.put(device, channel);
			identifiers.put(device, identifier);
			networkInterface.addNetworkListener(channel);
			chatapp.onDeviceDiscovery(device, identifier, channel);
		}
		
		System.out.println(device + ": " + identifier);
	}

	@Override
	public void onDeviceTimeout(InetAddress device) {
		networkInterface.removeNetworkListener(channels.get(device));
		channels.remove(device);
		identifiers.remove(device);
		chatapp.onDeviceTimeout(device);
	}
	
	@Override
	public void onReceive(InetAddress device, byte[] bytes) {
		chatapp.onReceive(device, bytes);
		System.out.println(device.toString() + ": " + new String(bytes));
	}
	
	@Override
	public void onMulticastReceive(InetAddress device, byte[] bytes) {
		System.out.println(device.toString() + ": " + new String(bytes));
		chatapp.onMulticastReceive(identifiers.get(device), bytes);
	}
	
	public HashMap<InetAddress, ReliableChannel> getChannels(){
		return channels;
	}
	
	public Main(ChatApp ca) {
		try{
			chatapp = ca;
			channels = new HashMap<>();
			identifiers = new HashMap<>();
			group = InetAddress.getByName("192.168.0.115");
			
			networkInterface = new NetworkInterface(group, port, InetAddress.getByName("192.168.0.115"));
			networkInterface.start();
			
			multicastChannel = new ReliableMulticastChannel(networkInterface);
			multicastChannel.start();
			networkInterface.addNetworkListener(multicastChannel);
			
			networkDiscovery = new NetworkDiscovery(networkInterface, "kappa");
			networkDiscovery.setNetworkDiscoveryListener(this);
			networkInterface.addNetworkListener(networkDiscovery);
		}catch(IOException e){
			System.out.println("Error in initializing application.Main()");
			System.out.println(e.getMessage());
		}
	}
}