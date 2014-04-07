import java.io.IOException;
import java.net.InetAddress;

import network.NetworkPacket;
import network.NetworkInterface;
import network.NetworkListener;


public class Main {
	public static void main(String[] args) throws IOException, InterruptedException {
		NetworkInterface networkInterface = new NetworkInterface();
		
		networkInterface.setListener(new NetworkListener() {
			@Override
			public void onReceive(NetworkPacket packet) {
				System.out.println(new String(packet.getData()));
			}
		});
		
		networkInterface.start();
		networkInterface.send(new NetworkPacket(InetAddress.getLocalHost(), InetAddress.getByName("130.89.131.74"), 2, "hoi".getBytes()));
	}
	
}