package transport.multicast;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Scanner;

import network.NetworkInterface;
import network.discovery.NetworkDiscovery;
import network.discovery.NetworkDiscoveryListener;

public class Main implements NetworkDiscoveryListener {
	private HashMap<InetAddress, String> devices;

	@Override
	public void onDeviceDiscovery(InetAddress device, String identifier) {
		devices.put(device, identifier);

		System.out.println(devices);
	}

	@Override
	public void onDeviceTimeout(InetAddress device) {
		devices.remove(device);

		System.out.println(devices);
	}

	public static void main(String[] args) throws IOException,
			InterruptedException {
		new Main();
	}

	public Main() throws IOException {
		devices = new HashMap<>();
		System.out.println("START");
		// 130.89.130.41
		// 130.89.130.15
		// 55555
		NetworkInterface networkInterface = new NetworkInterface(
				InetAddress.getByName("130.89.169.104"), 55554, InetAddress.getLocalHost());
		networkInterface.start();

		NetworkDiscovery networkDiscovery = new NetworkDiscovery(
				networkInterface, "yolo");
		networkDiscovery.setNetworkDiscoveryListener(this);

		networkInterface.addNetworkListener(networkDiscovery);

		WindowedChannel channel = new WindowedChannel(InetAddress.getByName("130.89.169.104"), networkInterface);

		networkInterface.addNetworkListener(channel);
		Scanner user = new Scanner(System.in);
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
				channel.getOutputStream()));

		while (true) {
			if (user.hasNextLine()) {
				String text = user.nextLine();
				if (text != null) {
					out.write(text);
					out.newLine();

					out.flush();
				}
			}
		}
	}

}