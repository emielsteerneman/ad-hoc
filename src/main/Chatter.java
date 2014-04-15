package main;

import gui.PrivateChatGUI;

import java.net.InetAddress;

import network.Protocol;
import transport.unicast.ReliableChannel;
import diffiehellman.DiffieHellmanProtocol;
import diffiehellman.PrimeGenerator;

public class Chatter {
	
	private PrivateChatGUI gui;
	private InetAddress device;
	private String identifier;
	private ReliableChannel channel;
	
	private keyExchanger keyExchanger;
	private long secretKey = -1;

	public Chatter(InetAddress device, String identifier, ReliableChannel channel){
		System.out.println("New chatter");
		this.device = device;
		this.identifier = identifier;
		this.channel = channel;		
		keyExchanger keyExchanger = new keyExchanger(this);
	}
	
	public void onReceive(byte[] bytes){
		String message;
		if(secretKey != -1){
			message = DiffieHellmanProtocol.decrypt(bytes, secretKey);
		}else{
			message = new String(bytes);
			keyExchanger.exchange();
		}
		
		String command = message.split("")[0];
		message = message.split(" ", 2)[1];
		
		switch(command){
			case Protocol.MESSAGE:
				gui.message(message);
				break;
			case Protocol.KEY_FROM_OTHER_CHATTER:
				keyExchanger.receive(message);
				break;
			default:
				System.out.println("Unknown protocol " + command + " " + message);
				break;
		}
	}
	
	public void send(String protocol, String message){
		channel.sendBytes((protocol + " " + message).getBytes());
	}
	
	
	public void openGUI(){
		gui = new PrivateChatGUI(identifier, this);
		if(secretKey == -1)
			keyExchanger.exchange();
	}
	
	public InetAddress getDevice(){
		return device;
	}
	
	public String getIdentifier(){
		return identifier;
	}
	
	public ReliableChannel getChannel(){
		return channel;
	}
	
	
	private class keyExchanger {
		Chatter chatter;
		boolean sent = false;

		long prime;
		int secretInt;
		long partialKeyOwn;
		long partialKeyOther;
		
		long secretKey;
		
		keyExchanger(Chatter chatter){
			this.chatter = chatter;
		}
		
		//Sending prime and key	
		public void exchange(){
			if(sent)
				return;
			
			long prime = PrimeGenerator.generatePrime();
			secretInt = (int)(Math.random() * 100);
			partialKeyOwn = DiffieHellmanProtocol.generatePartialKey(prime, secretInt);
			
			send(Protocol.KEY_FROM_OTHER_CHATTER, Long.toString(partialKeyOwn) + " " + Long.toString(prime));
			sent = true;
		}
		
		//Receiving prime and key
		public void receive(String message){
			partialKeyOther = new Long(message.split(" ")[0]);
			
			if(!sent){
				prime = new Long(message.split(" ")[1]);
				secretInt = (int)(Math.random() * 100);
				partialKeyOwn = DiffieHellmanProtocol.generatePartialKey(prime, secretInt);
				chatter.send(Protocol.KEY_FROM_OTHER_CHATTER, Long.toString(partialKeyOwn));
				sent = true;
			}
			
			secretKey = DiffieHellmanProtocol.calculateSecretKey(partialKeyOther, secretInt, prime);
			chatter.secretKey = secretKey;			
		}
	}
}