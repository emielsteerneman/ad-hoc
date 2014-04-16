package application;

import java.awt.Dimension;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.UIManager;

import network.NetworkDevice;
import network.NetworkInterface;
import network.discovery.NetworkDiscovery;
import network.discovery.NetworkDiscoveryListener;
import transport.multicast.ReliableMulticastChannelListener;
import transport.multicast.WindowedChannel;
import transport.unicast.ReliableChannel;
import transport.unicast.ReliableChannelListener;
import application.view.GroupChatViewListener;
import application.view.MainView;
import application.view.PrivateChatViewListener;


public class ChatClient implements GroupChatViewListener, PrivateChatViewListener, ReliableChannelListener, ReliableMulticastChannelListener, NetworkDiscoveryListener {
	private HashMap<NetworkDevice, ReliableChannel> channels;
	
	private WindowedChannel multicastChannel;
	
	private NetworkInterface networkInterface;
	private NetworkDiscovery networkDiscovery;
	
	private String localHost = "192.168.0.115";
	private String group = "192.168.0.115";
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
		
		mainView.addNetworkDevice(networkDevice);
	}

	@Override
	public void onDeviceTimeout(NetworkDevice networkDevice) {
		networkInterface.removeNetworkListener(channels.get(networkDevice));
		channels.remove(networkDevice);
		mainView.removeNetworkDevice(networkDevice);
	}
	
	@Override
	public void onReceive(InetAddress address, byte[] bytes) {
		String type = new String(new byte[]{bytes[0], bytes[1], bytes[2]});
		
		if (type.equals("MSG")) {
			mainView.newPrivateMessage(networkDiscovery.getNetworkDeviceByInetAddress(address), new String(bytes, 4, bytes.length - 4));
		} else if (type.equals("FIL")) {
			StringBuffer buf = new StringBuffer();
			
			int position = 5;
			
			while (position < bytes.length && (char) bytes[position] !=  '\"') {
				buf.append((char) bytes[position]);
				
				position++;
			}
			
			position++;
			position++;
			
			String fileName = buf.toString();
			
			File file = new File(fileName);
			
			try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
				out.write(Arrays.copyOfRange(bytes, position, bytes.length));
			} catch (IOException e) { }
			
			mainView.newPrivateMessage(networkDiscovery.getNetworkDeviceByInetAddress(address), "\"" + buf.toString() + "\"");
		}
	}
	
	@Override
	public void onMulticastReceive(InetAddress device, byte[] bytes) {
		System.out.println(device.toString() + ": " + new String(bytes));
	}
	
	@Override
	public void onPrivateMessageSend(NetworkDevice device, String message) {
		byte[] data = ("MSG " + message).getBytes();
		
		channels.get(device).sendBytes(data);
	}

	@Override
	public void onPrivateFileSend(NetworkDevice device, byte[] file, String filename) {		
		byte[] b = ("FIL \"" + filename + "\" ").getBytes(); 
		
		byte[] data = new byte[b.length + file.length];
		
		System.arraycopy(b, 0, data, 0, b.length);
		System.arraycopy(file, 0, data, b.length, file.length);
		
		channels.get(device).sendBytes(data);
	}
	
	@Override
	public void onGroupMessageSend(String message) {
		byte[] data = ("MSG " + message).getBytes();
		
		//multicastChannel.sendBytes(data);
		
		try {
			multicastChannel.getOutputStream().write(data);
			multicastChannel.getOutputStream().flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onGroupFileSend(byte[] file, String filename) {
		byte[] b = ("FIL \"" + filename + "\" ").getBytes(); 
		
		byte[] data = new byte[b.length + file.length];
		
		System.arraycopy(b, 0, data, 0, b.length);
		System.arraycopy(file, 0, data, b.length, file.length);
		
		//multicastChannel.sendBytes(data);
	}
	
	public ChatClient(String identifier) throws IOException {
		mainView = new MainView(identifier, this);
		
		JFrame frame = new JFrame("Ad hoc chat");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setMinimumSize(new Dimension(600, 480));
		frame.setVisible(true);
		frame.add(mainView);
		
		channels = new HashMap<>();
		
		networkInterface = new NetworkInterface(InetAddress.getByName(group), port, InetAddress.getByName(localHost));
		networkInterface.start();
		
		multicastChannel = new WindowedChannel(InetAddress.getByName(localHost), networkInterface);
		multicastChannel.addDeviceToChat(InetAddress.getByName(localHost));
		//multicastChannel.start();
		networkInterface.addNetworkListener(multicastChannel);
		
		networkDiscovery = new NetworkDiscovery(networkInterface, identifier);
		networkDiscovery.setNetworkDiscoveryListener(this);
		networkInterface.addNetworkListener(networkDiscovery);
	}
	
	public static void main(String[] args) throws IOException {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) { }
		
//		String alias = JOptionPane.showInputDialog(null, "Enter an alias");
		String alias = "Aede";
		
		new ChatClient(alias);
	}
	
}