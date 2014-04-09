
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetAddress;

import network.NetworkInterface;
import transport.ReliableChannel;


public class Main {
	public static void main(String[] args) throws IOException, InterruptedException {
		// 130.89.130.41
		// 130.89.130.15
		// 55555
		NetworkInterface networkInterface = new NetworkInterface(InetAddress.getByName("130.89.130.15"), 55555);
		networkInterface.start();
		
		ReliableChannel channel = new ReliableChannel(InetAddress.getByName("130.89.131.41"), InetAddress.getByName("190.89.131.15"), networkInterface);
		
		networkInterface.addNetworkListener(channel);
		
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(channel.getOutputStream()));
		
		out.write("adsfadsfjasdfjaskldfjlkasdjflkasjdfasdfasdfasdfasdfjasdfkjaskdjf;klasdjfklsadjfklasjfdkajsdfkasdfjaksldjfkalsdjfka");
		out.write("adsfadsfjasdfjaskldfjlkasdjflkasjdfasdfasdfasdfasdfjasdfkjaskdjf;klasdjfklsadjfklasjfdkajsdfkasdfjaksldjfkalsdjfka");
		out.write("adsfadsfjasdfjaskldfjlkasdjflkasjdfasdfasdfasdfasdfjasdfkjaskdjf;klasdjfklsadjfklasjfdkajsdfkasdfjaksldjfkalsdjfka");
		out.write("adsfadsfjasdfjaskldfjlkasdjflkasjdfasdfasdfasdfasdfjasdfkjaskdjf;klasdjfklsadjfklasjfdkajsdfkasdfjaksldjfkalsdjfka");
		
		out.newLine();
		out.flush();
	}
	
}