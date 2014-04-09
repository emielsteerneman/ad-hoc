
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
		
		ReliableChannel channel = new ReliableChannel(InetAddress.getByName("130.89.131.78"),InetAddress.getByName("190.89.131.74"), networkInterface);
		
		OutputStream out = channel.getOutputStream();
		byte[] s = new byte[200];
		byte[] d = new byte[200];
		for(int i=0; i<200; i++){
			s[i] = 1;
			d[i] = 2;
		}
		d[199] = '\n';
		s[199] = '\n';
		out.write(d);
		out.write(s);
		
		}
	
}