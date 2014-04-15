package transport.multicast;

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
import java.util.Collections;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import network.NetworkInterface;
import network.NetworkListener;
import network.NetworkPacket;
import transport.TransportPacket;

public class WindowedChannel implements NetworkListener {
	public static final int WNDSZ = 1;
	private static final int MSS = 10;
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
	private boolean expectNewStream = true;
	private boolean packetCountKnow = false;
	// private int packetCount;
	private ReliableMulticastChannelListener listener;
	private HashMap<InetAddress, Integer> addressIndex = new HashMap<InetAddress, Integer>();
	private ArrayList<ArrayList<Integer>> openSequences = new ArrayList<ArrayList<Integer>>();
	private ArrayList<TransportPacket> packetList = new ArrayList<TransportPacket>();
	private ArrayList<ArrayList<Byte>> tempFile = new ArrayList<ArrayList<Byte>>();
	private ArrayList<HashMap<Integer, byte[]>> integerSequencemap = new ArrayList<HashMap<Integer, byte[]>>();
	private ArrayList<Integer> packetCount;
	private ArrayList<Byte> streamNumber;

	// private ArrayList<TranportPacket>

	// private byte streamNumber = 0;

	public WindowedChannel(InetAddress address,
			NetworkInterface networkInterface) throws IOException {
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
		long DELAY = 500;
		this.timer.scheduleAtFixedRate(queueSender, DELAY, DELAY);
		// this.timer.scheduleAtFixedRate(new ackSimulator(), DELAY*2, DELAY*2);

	}

	public void setReliableChannelListener(
			ReliableMulticastChannelListener reliableChannelListener) {
		this.listener = reliableChannelListener;
	}

	// Reads the queue of the channel and sends data in a windows. continues
	// after every send packet is ack'ed
	private class QueueSender extends TimerTask {
		private int currentStream;

		private int sendIndex;
		private ArrayList<Integer> expectedACK;
		private ArrayList<TransportPacket> currentWindow;

		public void priorityPacket(TransportPacket packet) {
			if (currentWindow.size() > 0) {
				int i = 0;
				while (i < currentWindow.size()) {
					if (i < currentWindow.size()
							&& !currentWindow.get(i).isFlagSet(
									TransportPacket.ACK)) {
						currentWindow.set(i, packet);
						break;
					}
					i++;
				}
			} else {
				currentWindow.add(packet);
			}

		}

		public QueueSender(ArrayList<TransportPacket> queue) {
			expectedACK = new ArrayList<Integer>();
			currentWindow = new ArrayList<TransportPacket>();
		}

		/**
		 * Fill the sendWindow with n Packets to be send
		 */
		private void fillWindow() {
			int index = 0;
			if (currentWindow.size() == 0) {
				while (currentWindow.size() < WNDSZ && packetList.size() > 0
						&& index < packetList.size()) {

					TransportPacket t = null;
					// fill expectedACK with next seqs
					t = packetList.get(index);
					// Check whether packet has same stream number
					if (t.getStreamNumber() != this.currentStream) {

						System.out.println("");
						System.out.println("WINDOW: " + currentWindow.size());
						break;
					} else {
						// If not continue polling and adding expected ACK's
						// packetList.poll();

						if (!t.isFlagSet(TransportPacket.ACK)) {

							expectedACK.add(t.getSequenceNumber());
						}
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
			// synchronized (packetList) {
			// while (true) {
			// Try sending as long as there are packets left to send
			if (packetList.size() > 0 || currentWindow.size() > 0) {
				/**
				 * Check whether the first packet in the queue has a new
				 * streamIndex -> increment streamIndex Starts transmission of a
				 * new file\message
				 **/

				if (expectedACK.size() == 0
						&& packetList.size() > 0
						&& packetList.get(0).getStreamNumber() != this.currentStream) {
					currentStream++;

				}
				this.fillWindow();

				// If all packets have been ack'ed, read load next packets
				// for in send queue
				//

				// System.out.println("");
				if (currentWindow.size() > 0) {

					if (sendIndex < currentWindow.size()) {

						NetworkPacket networkPacket = new NetworkPacket(
								networkInterface.getLocalHost(), address, (byte) 2, currentWindow
										.get(sendIndex).getBytes());
						networkPacket.setFlags(NetworkPacket.TRANSPORT);

						// Check whether ACK has been removed from the list
						// of
						// expected ACKS or packet is an ACK packet

						if (expectedACK.contains(currentWindow.get(sendIndex)
								.getSequenceNumber())
								|| currentWindow.get(sendIndex).isFlagSet(
										TransportPacket.ACK)) {

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
								if (packetList.size() > 0) {
									// System.out.println("Removing");
									packetList.remove(0);
								}
							}
							currentWindow.clear();

							// REMOVE PACKETS FROM LIST

							// currentStream++;
						} else {
							// Resent remaining packets

							// DEBUG: remove first last entry from ACK list.
							// expectedACK.remove(expectedACK.size() - 1);

						}
					}
				}
			}
			// }

			// }

		}

		public void receivedACK(int workIndex, int ack) {

			int index = expectedACK.indexOf(ack);
			if (index > -1) {
				expectedACK.remove(index);
			}

		}

	}

	private class InputHandler extends Thread {
		BufferedReader in;
		int seqNumber;
		byte streamCounter;

		public InputHandler(InputStream in) {
			this.in = new BufferedReader(new InputStreamReader(in));
		}

		public void run() {
			while (true) {

				try {
					byte[] data = in.readLine().getBytes();
					int dataPosition = 0;
					if (data.length > 0) {
						
						ArrayList<TransportPacket> temp = new ArrayList<TransportPacket>();
						while (data.length - dataPosition > MSS) {
							
							// System.out.println(data.length + ", " +
							// dataPosition
							// + " -- " + MSS);

							byte[] packetData = Arrays.copyOfRange(data,
									dataPosition, dataPosition + MSS);

							TransportPacket transportPacket = new TransportPacket(
									packetData);
							// Set packet data
							transportPacket.setStreamNumber(streamCounter);
							transportPacket.setSequenceNumber(seqNumber);
							// transportPacket.setAcknowledgeNumber(seqNumber);
							temp.add(transportPacket);

							dataPosition += MSS;
							seqNumber++;
							System.out.println(seqNumber);
						}
						if (dataPosition < data.length) {
							byte[] packetData = new byte[data.length
									- dataPosition];

							System.arraycopy(data, dataPosition, packetData, 0,
									packetData.length);

							TransportPacket transportPacket = new TransportPacket(
									packetData);
							// Set packet data
							transportPacket.setStreamNumber(streamCounter);
							transportPacket.setSequenceNumber(seqNumber);
							transportPacket.setAcknowledgeNumber(seqNumber);
							temp.add(transportPacket);

						}
						// SET FLAG for last packet in list to mark end of file

						temp.get(temp.size() - 1).setFlags(
								TransportPacket.FRAGMENTED);

						// for (TransportPacket p : temp) {
						// p.setPacketCount(temp.size());
						// }
						packetList.addAll(temp);
						seqNumber++;
						//
						seqNumber = 0;
						streamCounter++;
	
					}
				} catch (IOException e) {
				}
			}
		}
	}

	public OutputStream getOutputStream() {
		return out;
	}

	public InputStream getInputStream() {
		return in;
	}

	public void addDeviceToChat(InetAddress address) {
		this.addressIndex.put(address, tempFile.size());
		openSequences.add(new ArrayList<Integer>());
		tempFile.add(new ArrayList<Byte>());
		integerSequencemap.add(new HashMap<Integer, byte[]>());
		

	}

	public byte[] parseFile(int workIndex, ArrayList<Byte> bytes) {
		int length = 0;
		ArrayList<Integer> keys = new ArrayList<Integer>();
		keys.addAll(integerSequencemap.get(workIndex).keySet());
		Collections.sort(keys);
		ArrayList<Byte> file = new ArrayList<Byte>();

		for (int i : keys) {

			byte[] temp = integerSequencemap.get(workIndex).get(i);
			for (int a = 0; a < temp.length; a++) {
				file.add(temp[a]);
			}

		}
		length = file.size();
		byte[] ret = new byte[length];
		for (int i = 0; i < length; i++) {
			ret[i] = file.get(i);
		}

		return ret;

	}

	public void addBytesToFile(ArrayList<Byte> bytes, byte[] data) {
		for (byte b : data) {
			bytes.add(b);
			// System.out.print(b + " ");
		}
		// System.out.println(new String(data));
		System.out.println("currentfile size: " + bytes.size());
	}

	@Override
	public void onReceive(NetworkPacket packet) {
		int workIndex = addressIndex.get(packet.getSourceAddress());
		System.out.println(workIndex);
		if (packet.isFlagSet(NetworkPacket.TRANSPORT) && workIndex > 0) {

			TransportPacket received = TransportPacket.parseBytes(packet
					.getData());
			if (received != null) {

				if (received.isFlagSet(TransportPacket.ACK)) {

					queueSender.receivedACK(workIndex,
							received.getAcknowledgeNumber());
					// React to ACK
				} else {
					// IF ACK field == -1 -> data packet
					// -> add to queue and send ack

					// Read how many packets have to be expected
					int seq = received.getSequenceNumber();
					TransportPacket transportPacket = new TransportPacket(0,
							received.getAcknowledgeNumber(),
							TransportPacket.ACK, received.getStreamNumber(),
							new byte[0]);
					//

					//
					transportPacket.setAcknowledgeNumber(seq);
					// queueSender.priorityPacket(transportPacket);
					// packetList.add(transportPacket);
					queueSender.priorityPacket(transportPacket);
					if (expectNewStream) {

						integerSequencemap.get(workIndex).clear();

						expectNewStream = false;
						packetCountKnow = false;
					}

					if (received.getStreamNumber() == this.streamNumber
							.get(workIndex)
							&& !integerSequencemap.get(workIndex).containsKey(
									seq)) {

						integerSequencemap.get(workIndex).put(seq,
								received.getData());
						if (received.isFlagSet(TransportPacket.FRAGMENTED)) {
							packetCountKnow = true;
							packetCount.set(workIndex, seq + 1);

						}
						// System.out.println("Received :"+seq);

					}

					if (packetCountKnow
							&& integerSequencemap.get(workIndex).size() == packetCount
									.get(workIndex)) {
						listener.onMulticastReceive(packet.getSourceAddress(),
								parseFile(workIndex, tempFile.get(workIndex)));
						tempFile.get(workIndex).clear();
						expectNewStream = true;
						byte t = streamNumber.get(workIndex);
						streamNumber.set(workIndex, t);

						// Set packet data
						// Add received data to temporary file

					}
				}
			}
			// }

		}
	}

}
