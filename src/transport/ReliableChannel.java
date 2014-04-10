package transport;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import network.NetworkInterface;
import network.NetworkListener;
import network.NetworkPacket;

public class ReliableChannel implements NetworkListener {
	public static final int WNDSZ = 5;
	private static final int MSS = 5;
	private InetAddress localAddress;
	private InetAddress address;
	private NetworkInterface networkInterface;

	private OutputStream out;
	private InputStream in;

	private PipedOutputStream pipedOut;
	private PipedInputStream pipedIn;
	private InputHandler inputHandler;
	private QueueSender queueSender;
	private Timer timer;
	// QUEUE for important packets
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

		inputHandler = new InputHandler(pipedIn);
		Thread t = (Thread) inputHandler;
		t.start();
		queueSender = new QueueSender(packetList);
		this.timer = new Timer();
		long DELAY = 150;
		this.timer.scheduleAtFixedRate(queueSender, DELAY, DELAY);
//		this.timer.scheduleAtFixedRate(new ackSimulator(), DELAY*2, DELAY*2);

		
	}

	// Reads the queue of the channel and sends data in a windows. continues
	// after every send packet is ack'ed
	private class QueueSender extends TimerTask {
		private ArrayList<TransportPacket> sendQueue;
		private int currentStream;

		private int sendIndex;
		private ArrayList<Integer> expectedACK;
		private ArrayList<TransportPacket> currentWindow;

		public void priorityPacket(TransportPacket packet) {
			if (currentWindow.size() > 0) {
				currentWindow.set(0, packet);
			} else {
				currentWindow.add(packet);
			}
			System.out.println("NEW PRIORITY " + currentWindow.size());
		}

		public QueueSender(ArrayList<TransportPacket> queue) {
			this.sendQueue = queue;
			expectedACK = new ArrayList<Integer>();
			currentWindow = new ArrayList<TransportPacket>();
		}

		/**
		 * Fill the sendWindow with n Packets to be send
		 */
		private void fillWindow() {
			int index = 0;
			if (currentWindow.size() == 0) {
				while (currentWindow.size() < WNDSZ && sendQueue.size() > 0
						&& index < sendQueue.size()) {
					// System.out.print("filling window--");
					TransportPacket t = null;
					// fill expectedACK with next seqs
					t = sendQueue.get(index);
					// Check whether packet has same stream number
					if (t.getStreamNumber() != this.currentStream) {
						break;
					} else {
						// If not continue polling and adding expected ACK's
						// sendQueue.poll();
						expectedACK.add(t.getAcknowledgeNumber());
						currentWindow.add(t);
						// Reset sendIndex to 0 to start at the beginning of
						// each window
						sendIndex = 0;

					}
					index++;
				}
			}

		}

		@Override
		// TODO: ACK, set ack/seq numbers of transportPackets, priority packets
		// (replace first packet in send queue)
		public synchronized void run() {
//			while (true) {
				// Try sending as long as there are packets left to send
				if (sendQueue.size() > 0 || expectedACK.size() > 0) {

					// Check whether the first packet in the queue has a new
					// streamIndex -> increment streamIndex
					// Starts transmission of a new file\message
					if (expectedACK.size() == 0
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

					// System.out.println("");
					if (expectedACK.size() > 0) {

						if (sendIndex < expectedACK.size()) {

							NetworkPacket networkPacket = new NetworkPacket(
									localAddress, address, (byte) 2,
									currentWindow.get(sendIndex).getBytes());
							networkPacket.setFlags(NetworkPacket.TRANSPORT_FLAG);
							// Check whether ACK has been removed from the list
							// of expected ACKS
							if (expectedACK.contains(currentWindow.get(
									sendIndex).getAcknowledgeNumber())) {
								System.out.println("SEQ: "
										+ currentWindow.get(sendIndex)
												.getSequenceNumber());
								try {
									networkInterface.send(networkPacket);

								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
							sendIndex++;
						} else {
							// check with packets have not been acked

							// clear for debug
							sendIndex = 0;
							// expectedACK.clear();

							if (expectedACK.size() == 0) {
								// IF list empty: all packets have been acked ->
								// new Stream
								// System.out.println("window empty. removing send packets from list");
								for (int i = 0; i < currentWindow.size(); i++) {
									if (sendQueue.size() > 0) {
										sendQueue.remove(0);
									}
								}
								currentWindow.clear();

								// REMOVE PACKETS FROM LIST

								// currentStream++;
							} else {
								// Resent remaining packets

								// DEBUG: remove first last entry from ACK list.
								// expectedACK.remove(expectedACK.size() - 1);
								System.out.println("--------------------");
							}
						}
					}
				}

//			}

		}

		public void receivedACK(int seq, int ack) {
			int index = expectedACK.indexOf(ack);
			if (index > -1) {
				System.out.println("ACK: " + ack);
				expectedACK.remove(index);
			}
		}

	}

	private class InputHandler extends Thread {
		BufferedReader in;
		int seqNumber;

		public InputHandler(InputStream in) {
			this.in = new BufferedReader(new InputStreamReader(in));
		}

		public void run() {
			while (true) {
				// if(in.)
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
						// Set packet data
						transportPacket.setStreamNumber(streamNumber);
						transportPacket.setSequenceNumber(seqNumber);
						transportPacket.setAcknowledgeNumber(seqNumber);
						packetList.add(transportPacket);

						dataPosition += MSS;
						seqNumber++;
					}
					if (dataPosition < data.length) {
						byte[] packetData = new byte[data.length - dataPosition];

						System.arraycopy(data, dataPosition, packetData, 0,
								packetData.length);

						TransportPacket transportPacket = new TransportPacket(
								packetData);
						// Set packet data
						transportPacket.setStreamNumber(streamNumber);
						transportPacket.setSequenceNumber(seqNumber);
						transportPacket.setAcknowledgeNumber(seqNumber);
						packetList.add(transportPacket);

					}
					seqNumber++;
				} catch (IOException e) {
				}
				//
				seqNumber = 0;
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

	@Override
	public void onReceive(NetworkPacket packet) {
		System.out.println("INCOMMING!");
		// Check whether incoming packet is for local ip
		System.out.println(address.toString() + " - "
				+ packet.getSourceAddress().toString());
		if (packet.getSourceAddress().equals(address)&&packet.isFlagSet(NetworkPacket.TRANSPORT_FLAG)) {
			System.out.println("HERE");
			// Check whether packet is an ACK
			TransportPacket received = TransportPacket.parseBytes(packet
					.getData());
			System.out.print("RECEIVED: ");
			for(byte b : received.getBytes()){
				System.out.print(b+" ");
			}
			System.out.println("");
			if (received != null) {
				if (received.isFlagSet(TransportPacket.ACK_FLAG)) {
					System.out.println("GOT ACK "
							+ received.getAcknowledgeNumber());
					// React to ACK
				} else{
					// IF ACK field == -1 -> data packet
					// -> add to queue and send ack

					TransportPacket transportPacket = new TransportPacket(0,
							received.getAcknowledgeNumber(),
							TransportPacket.ACK_FLAG,
							received.getStreamNumber(), new byte[0]);
					//
					transportPacket.setAcknowledgeNumber(received.getAcknowledgeNumber());
					//
					System.out.println("SENDING ACK: "
							+ received.getSequenceNumber());
					System.out.print("ACK_PACK: ");
					byte[] packetBytes = transportPacket.getBytes();
					for(byte b: packetBytes){
						System.out.print(b);
					}
					System.out.println("");
					// queueSender.priorityPacket(transportPacket);
					packetList.add(transportPacket);

					// Set packet data

				}
			}
		}

	}

	public class ackSimulator extends TimerTask {
		@Override
		public synchronized void run() {
//			while (true) {
				if (queueSender.expectedACK.size() > 0) {
					System.out.print("EXP: ");
					for(int i : queueSender.expectedACK){
						System.out.print(i+",");
					}
					System.out.println();
					TransportPacket pac = new TransportPacket(new byte[0]);
					pac.setAcknowledgeNumber(queueSender.expectedACK.get(0));
					onReceive(new NetworkPacket(address, localAddress, (byte)1, pac.getBytes()));
//					queueSender.receivedACK(0, queueSender.expectedACK.get(0));
				}

//			}
		}
	}
	public static void main(String[] args) throws UnknownHostException, IOException{
		NetworkInterface networkInterface = new NetworkInterface(InetAddress.getByName("130.89.130.41"), 55555);
		networkInterface.start();
//		
//		NetworkDiscovery networkDiscovery = new NetworkDiscovery(networkInterface, "yolo");
//		networkDiscovery.setNetworkDiscoveryListener(this);
//		
//		networkInterface.addNetworkListener(networkDiscovery);
		
		
		ReliableChannel channel = new ReliableChannel(InetAddress.getByName("130.89.130.41"), InetAddress.getByName("130.89.130.15"), networkInterface);

		networkInterface.addNetworkListener(channel);
		
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(channel.getOutputStream()));
		
		out.write(new String(new byte[1000]));
		out.newLine();
		out.flush();
	}

}
