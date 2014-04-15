package transport.unicast;

import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;

import transport.TransportPacket;


public class SimpleTransportProtocol implements TransportProtocol, Runnable {
	private static final long TIME_OUT = 100;
	
	private ReliableChannel channel;
	
	private LinkedBlockingQueue<TransportPacket> sendQueue;
	private boolean waitingForAcknowledgement = false;
	
	private int sequenceNumber = 0;
	private int lastAcknowledgeNumberSent = 0;
	private int acknowledgeNumberExpected = 0;
	
	private ByteBuffer buffer;
	
	private Timer sendTimer;
	private SendTask sendTask;
	
	private class SendTask extends TimerTask {
		private TransportPacket transportPacket;
		
		public SendTask(TransportPacket transportPacket) {
			this.transportPacket = transportPacket;
		}
		
		@Override
		public void run() {
			channel.sendTransportPacket(transportPacket);
		}
		
	}
	
	public SimpleTransportProtocol(ReliableChannel channel) {
		this.channel = channel;
		this.sendQueue = new LinkedBlockingQueue<>();
		this.buffer = ByteBuffer.allocate(10485760);
		this.sendTimer = new Timer();
	}
	
	@Override
	public void run() {
		while (true) {
			synchronized (sendQueue) {
				while (waitingForAcknowledgement) {
					try {
						sendQueue.wait();
					} catch (InterruptedException e) { }
				}
				
				if (!waitingForAcknowledgement && sendQueue.peek() != null) {
					TransportPacket transportPacket = sendQueue.peek();
					
					transportPacket.setSequenceNumber(sequenceNumber);
					sequenceNumber += transportPacket.getLength();
					
					acknowledgeNumberExpected = transportPacket.getSequenceNumber() + transportPacket.getLength();
					waitingForAcknowledgement = true;
					
					sendTask = new SendTask(transportPacket);
					sendTimer.scheduleAtFixedRate(sendTask, 0L, TIME_OUT);
				}
			}
		}
	}
	
	@Override
	public synchronized void onReceive(TransportPacket transportPacket) {
		if (transportPacket.isFlagSet(TransportPacket.ACK)) {
			if (TransportPacket.compareSequenceNumbers(acknowledgeNumberExpected, transportPacket.getAcknowledgeNumber()) == 0) {
				synchronized (sendQueue) {
					sendTask.cancel();
					waitingForAcknowledgement = false;
					sendQueue.poll();
					sendQueue.notify();
				}
			}
		} else {
			if (TransportPacket.compareSequenceNumbers(transportPacket.getSequenceNumber(), lastAcknowledgeNumberSent) < 0) {
				TransportPacket ackPacket = new TransportPacket(new byte[0]);
				ackPacket.setSequenceNumber(transportPacket.getAcknowledgeNumber());
				ackPacket.setAcknowledgeNumber(transportPacket.getSequenceNumber() + transportPacket.getLength());
				ackPacket.setFlags(TransportPacket.ACK);
				
				channel.sendTransportPacket(ackPacket);
			} else if (TransportPacket.compareSequenceNumbers(transportPacket.getSequenceNumber(), lastAcknowledgeNumberSent) == 0) {
				buffer.put(transportPacket.getData(), 0, transportPacket.getLength());
				
				if (!transportPacket.isFlagSet(TransportPacket.FRAGMENTED) || 
						(transportPacket.isFlagSet(TransportPacket.FRAGMENTED) && 
						!transportPacket.isFlagSet(TransportPacket.MORE_FRAGMENTS))) {
					
					byte[] bytes = new byte[buffer.position()];
					
					buffer.flip();
					buffer.get(bytes);
					buffer.clear();
					
					channel.receiveBytes(bytes);
				}
				
				lastAcknowledgeNumberSent = transportPacket.getSequenceNumber() + transportPacket.getLength();
				
				TransportPacket ackPacket = new TransportPacket(new byte[0]);
				ackPacket.setSequenceNumber(transportPacket.getAcknowledgeNumber());
				ackPacket.setAcknowledgeNumber(transportPacket.getSequenceNumber() + transportPacket.getLength());
				ackPacket.setFlags(TransportPacket.ACK);
				
				channel.sendTransportPacket(ackPacket);
			}
		}
	}

	@Override
	public void addToQueue(TransportPacket transportPacket) {
		synchronized (sendQueue) {
			sendQueue.add(transportPacket);
			sendQueue.notify();
		}
	}
	
}
