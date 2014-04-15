package main;

import gui.PrivateChatGUI;

import java.net.InetAddress;

import transport.unicast.ReliableChannel;

public class Chatter {
	
	private PrivateChatGUI gui;
	private InetAddress device;
	private String identifier;
	private ReliableChannel channel;
	
	public Chatter(InetAddress device, String identifier, ReliableChannel channel){
		System.out.println("New chatter");
		this.device = device;
		this.identifier = identifier;
		this.channel = channel;		
	}
	
	public void onReceive(){
		
	}
	
	public void openGUI(){
		gui = new PrivateChatGUI(identifier);
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
}
