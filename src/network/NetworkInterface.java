package network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;

public class NetworkInterface extends Thread {
	private static final int PORT = 4446;
	private static final String GROUP = "226.2.2.2";
	private static final int BUFFER_SIZE = 256;
	
	private MulticastSocket receiveSocket = null;
	private DatagramSocket sendSocket = null;
	private NetworkListener listener = null;
	
	public NetworkInterface() throws IOException {
		receiveSocket = new MulticastSocket(PORT);
		receiveSocket.joinGroup(InetAddress.getByName(GROUP));
		
		sendSocket = new DatagramSocket();
	}
	
	public void setListener(NetworkListener listener) {
		this.listener = listener;
	}
	
	@Override
	public void run() {
		boolean running = true;
		
		while (running) {
			DatagramPacket packet = new DatagramPacket(new byte[BUFFER_SIZE], BUFFER_SIZE);
			
			try {
				receiveSocket.receive(packet);
				
				if (listener != null) {
					try {
						receive(packet);
					} catch (IOException e){ 
						
					}
				}
			} catch (IOException e) {
				running = false;
			}
		}
		
		receiveSocket.close();
	}
	
	public void send(NetworkPacket networkPacket) throws IOException {
		byte[] packetData = new byte[NetworkPacket.HEADER_SIZE + networkPacket.getLength()];
		
		byte[] sourceAddress = networkPacket.getSourceAddress().getAddress();
		byte[] destinationAddress = networkPacket.getDestinationAddress().getAddress();
		int hopcount = networkPacket.getHopcount();
		byte[] data = networkPacket.getData();
		
		System.arraycopy(sourceAddress, 0, packetData, 0, 4);
		System.arraycopy(destinationAddress, 0, packetData, 4, 4);
		
		packetData[8]  = (byte) (hopcount >> 24);
		packetData[9]  = (byte) (hopcount >> 16);
		packetData[10] = (byte) (hopcount >>  8);
		packetData[11] = (byte) (hopcount >>  0);
		
		System.arraycopy(data, 0, packetData, 12, data.length);
		
		DatagramPacket packet = new DatagramPacket(packetData, packetData.length, InetAddress.getByName(GROUP), PORT);
		
		sendSocket.send(packet);
	}
	
	private void receive(DatagramPacket packet) throws IOException {
		byte[] packetData = packet.getData();
		
		InetAddress sourceAddress = InetAddress.getByAddress(Arrays.copyOfRange(packetData, 0, 4));
		InetAddress destinationAddress = InetAddress.getByAddress(Arrays.copyOfRange(packetData, 4, 8));
		
		int hopcount = (packetData[8]  << 24) +
				       (packetData[9]  << 16) +
				       (packetData[10] <<  8) +
				       (packetData[11] <<  0);
		
		byte[] data = Arrays.copyOfRange(packetData, 12, packet.getLength());
		
		if (sourceAddress != null && destinationAddress != null) {
			if (!destinationAddress.equals(InetAddress.getLocalHost())) {
				if (sourceAddress.equals(InetAddress.getLocalHost())) {
					return;
				} 
				
				if (hopcount > 0){
					hopcount--;
					
					send(new NetworkPacket(sourceAddress, destinationAddress, hopcount, data));
				} else {
					return;
				}
			} else {
				if (hopcount == 0) {
					listener.onReceive(new NetworkPacket(sourceAddress, destinationAddress, hopcount, data));
				} else {
					return;
				}
			}
		}
	}
	
}
