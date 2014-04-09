import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;

import network.NetworkInterface;
import network.routing.SimpleRoutingProtocol;
import transport.ReliableChannel;


public class Main {
	public static void main(String[] args) throws IOException, InterruptedException {
		NetworkInterface networkInterface = new NetworkInterface();
		networkInterface.setRoutingProtocol(new SimpleRoutingProtocol());
		
		ReliableChannel channel = new ReliableChannel(InetAddress.getByName("130.89.131.74"), networkInterface);
		
		OutputStream out = channel.getOutputStream();
		
		out.write("	 hallo\n".getBytes());
	}
	
}