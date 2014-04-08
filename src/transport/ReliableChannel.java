package transport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.InetAddress;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import network.NetworkInterface;

public class ReliableChannel {
	private static final int MSS = 10;
	
	private InetAddress address;
	private NetworkInterface networkInterface;
	
	private OutputStream out;
	private InputStream in;
	
	private PipedOutputStream pipedOut;
	private PipedInputStream pipedIn;
	
	private Queue<TransportPacket> sendQueue = new ArrayBlockingQueue<TransportPacket>(10);
	
	private byte streamNumber = 0;
	
	public ReliableChannel(InetAddress address, NetworkInterface networkInterface) throws IOException {
		this.address = address;
		this.networkInterface = networkInterface;
		
		pipedOut = new PipedOutputStream();
		in = new PipedInputStream(pipedOut);
		
		pipedIn = new PipedInputStream();
		out = new PipedOutputStream(pipedIn);
		
		new InputHandler(pipedIn).start();
	}
	
	private class InputHandler extends Thread {
		BufferedReader in;
		
		public InputHandler(InputStream in) {
			this.in = new BufferedReader(new InputStreamReader(in));
		}
		
		public void run() {
			while (true) {
				try {
					byte[] data = in.readLine().getBytes();
					int dataPosition = 0;
					
					while (data.length - dataPosition > MSS) {
						byte[] packetData = new byte[MSS];
						
						System.arraycopy(data, dataPosition * MSS, packetData, 0, packetData.length);
						
						TransportPacket transportPacket = new TransportPacket(packetData);
						transportPacket.setStreamNumber(streamNumber);
						
						sendQueue.add(transportPacket);
						
						dataPosition += MSS;
					}	
					
					if (dataPosition < data.length) {
						byte[] packetData = new byte[data.length - dataPosition];
					
						System.arraycopy(data, dataPosition, packetData, 0, packetData.length);
					
						TransportPacket transportPacket = new TransportPacket(packetData);
						transportPacket.setStreamNumber(streamNumber);
						
						sendQueue.add(transportPacket);
					}
				} catch (IOException e) { }
				
				streamNumber++;
			}
		}
	}
	
	public OutputStream getOutputStream() {
		return out;
	}
	
	public InputStream getInputStream() {
		return in;
	}
	
}
