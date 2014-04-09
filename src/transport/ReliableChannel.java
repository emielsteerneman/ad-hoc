package transport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import network.NetworkInterface;
import network.NetworkPacket;

public class ReliableChannel {
	public static final int WNDSZ = 10;
	private static final int MSS = 5;
	private InetAddress localAddress;
	private InetAddress address;
	private NetworkInterface networkInterface;

	private OutputStream out;
	private InputStream in;

	private PipedOutputStream pipedOut;
	private PipedInputStream pipedIn;
	//QUEUE for important packets 
	private ArrayList<TransportPacket> packetList = new ArrayList<TransportPacket>(); 
	// private ArrayList<TranportPacket>

	private byte streamNumber = 0;

	public ReliableChannel(InetAddress localAddress, InetAddress address,
			NetworkInterface networkInterface) throws IOException {
		this.localAddress = localAddress;
		this.address = address;
		this.networkInterface = networkInterface;

		pipedOut = new PipedOutputStream();
		in = new PipedInputStream(pipedOut);

		pipedIn = new PipedInputStream();
		out = new PipedOutputStream(pipedIn);

		new InputHandler(pipedIn).start();
		new QueueSender(packetList).start();
	}

	// Reads the queue of the channel and sends data in a windows. continues
	// after every send packet is ack'ed
	private class QueueSender extends Thread {
		private ArrayList<TransportPacket> sendQueue;
		private int currentStream;
		private int sendIndex;
		private ArrayList<Integer> expectedACK;
		private ArrayList<NetworkPacket> currentWindow;

		public QueueSender(ArrayList<TransportPacket> queue) {
			this.sendQueue = queue;
			expectedACK = new ArrayList<Integer>();
			currentWindow = new ArrayList<NetworkPacket>();
		}
		/**
		 * Fill the sendWindow with n Packets to be send
		 */
		private void fillWindow(){
			int index = 0;
			while (currentWindow.size() < WNDSZ && sendQueue.size() > 0) {
				// System.out.print("filling window--");
				TransportPacket t = null;
				// fill expectedACK with next seqs
				t = sendQueue.get(index);
				// Check whether packet has same stream number
				if (t.getStreamNumber() != this.currentStream) {
					break;
				} else {
					// If not continue polling and adding expected ACK's
//					sendQueue.poll();
					
					expectedACK.add(t.getAcknowledgeNumber());
					
					NetworkPacket networkPacket = new NetworkPacket(
							localAddress, address, (byte) 2,
							t.getBytes());
					
					currentWindow.add(networkPacket);
					// Reset sendIndex to 0 to start at the beginning of
					// each window
					sendIndex = 0;
					
					System.out.print(currentWindow.size() + ", ");
				}
			}
		}
		@Override
		// TODO: ACK, set ack/seq numbers of transportPackets, priority packets (replace first packet in send queue)
		public void run() {
			while (true) {
				// Try sending as long as there are packets left to send
				if (sendQueue.size() > 0 || currentWindow.size() > 0) {

					// Check whether the first packet in the queue has a new
					// streamIndex -> increment streamIndex
					// Starts transmission of a new file\message
					if (currentWindow.size() == 0
							&& sendQueue.get(0).getStreamNumber() != this.currentStream) {
						currentStream++;
						System.out.println("                   NEW STREAM: "
								+ currentStream);
					}
					this.fillWindow();
					// System.out.println("queue: "+
					// sendQueue.size()+", window"+currentWindow.size());
					// If all packets have been ack'ed, read load next packets
					// for in send queue
					//

					System.out.println("");
					if (currentWindow.size() > 0) {
						if (sendIndex < currentWindow.size()) {

							System.out.println("packet" + (sendIndex + 1)
									+ " | " + currentWindow.size());
							try {
								networkInterface.send(currentWindow
										.get(sendIndex));
								sendIndex++;
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						} else {
							// check with packets have not been acked

							// clear for debug
							expectedACK.clear();

							if (expectedACK.size() == 0) {
								// IF list empty: all packets have been acked ->
								// new Stream
								System.out.println("window empty. removing send packets from list");
								for(int i=0; i<currentWindow.size();i++){
									sendQueue.remove(0);
								}
								currentWindow.clear();
								
								//REMOVE PACKETS FROM LIST

								
								
								
								// currentStream++;
							} else {
								// Resent remaining packets
							}
						}
					}
				}
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}

		public void receivedACK(int seq, int ack) {

		}

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
						// System.out.println(data.length + ", " + dataPosition
						// + " -- " + MSS);

						byte[] packetData = Arrays.copyOfRange(data,
								dataPosition, dataPosition + MSS);

						TransportPacket transportPacket = new TransportPacket(
								packetData);
						transportPacket.setStreamNumber(streamNumber);

						packetList.add(transportPacket);

						dataPosition += MSS;
					}

					if (dataPosition < data.length) {
						byte[] packetData = new byte[data.length - dataPosition];

						System.arraycopy(data, dataPosition, packetData, 0,
								packetData.length);

						TransportPacket transportPacket = new TransportPacket(
								packetData);
						transportPacket.setStreamNumber(streamNumber);

						packetList.add(transportPacket);
					}
				} catch (IOException e) {
				}

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
