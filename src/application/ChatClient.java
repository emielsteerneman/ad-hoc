package application;

import java.awt.Dimension;
import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.UIManager;

import network.NetworkDevice;
import network.NetworkInterface;
import network.discovery.NetworkDiscovery;
import network.discovery.NetworkDiscoveryListener;
import transport.multicast.ReliableMulticastChannel;
import transport.multicast.ReliableMulticastChannelListener;
import transport.unicast.ReliableChannel;
import transport.unicast.ReliableChannelListener;
import application.view.MainView;


public class ChatClient implements ReliableChannelListener, ReliableMulticastChannelListener, NetworkDiscoveryListener {
	private HashMap<NetworkDevice, ReliableChannel> channels;
	
	private ReliableMulticastChannel multicastChannel;
	
	private NetworkInterface networkInterface;
	private NetworkDiscovery networkDiscovery;
	
	private String localHost = "1.0.0.0.";
	private String group = "1.0.0.0.";
	private int port = 6666;
	
	private MainView mainView;
	
	@Override
	public void onDeviceDiscovery(NetworkDevice networkDevice) {
		ReliableChannel channel = null;
		
		try {
			 channel = new ReliableChannel(networkDevice.getAddress(), networkInterface);
		} catch (IOException e) { }
		
		if (channel != null) {
			channel.setReliableChannelListener(this);
			channel.start();
			
			channels.put(networkDevice, channel);
			networkInterface.addNetworkListener(channel);
		}
		
		System.out.println(networkDevice);
	}

	@Override
	public void onDeviceTimeout(NetworkDevice networkDevice) {
		networkInterface.removeNetworkListener(channels.get(networkDevice));
		channels.remove(networkDevice);
	}
	
	@Override
	public void onReceive(InetAddress device, byte[] bytes) {
		System.out.println(device.toString() + ": " + new String(bytes));
	}
	
	@Override
	public void onMulticastReceive(InetAddress device, byte[] bytes) {
		System.out.println(device.toString() + ": " + new String(bytes));
	}
	
	public ChatClient() throws IOException {
		mainView = new MainView();
		
		JFrame frame = new JFrame("Ad hoc chat");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setMinimumSize(new Dimension(600, 480));
		frame.setVisible(true);
		frame.add(mainView);
		
		channels = new HashMap<>();
		
		networkInterface = new NetworkInterface(InetAddress.getByName(group), port, InetAddress.getByName(localHost));
		networkInterface.start();
		
		multicastChannel = new ReliableMulticastChannel(networkInterface);
		multicastChannel.start();
		networkInterface.addNetworkListener(multicastChannel);
		
		networkDiscovery = new NetworkDiscovery(networkInterface, "kappa");
		networkDiscovery.setNetworkDiscoveryListener(this);
		networkInterface.addNetworkListener(networkDiscovery);
	}
	
	public static void main(String[] args) throws IOException {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) { }
		
		new ChatClient();
	}
	
}