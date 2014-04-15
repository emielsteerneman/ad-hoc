package main;

import gui.GUI;
import gui.PrivateChatGUI;
import gui.nicknameAsk;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;

import socket.SocketHandler;
import transport.unicast.ReliableChannel;
import application.Main;
import diffiehellman.DiffieHellmanProtocol;

public class ChatApp {

	private GUI gui;
	private SocketHandler sh;
	private DiffieHellmanProtocol dhp;
	private Main network;
	private boolean connect;
	
	private String username;
	private HashMap<InetAddress, Chatter> chatters;

	public ChatApp(String username) {
		this.username = username;
		gui = new GUI(username, sh, this);
		sh = new SocketHandler(gui, this);
		network = new Main(this);
		chatters = new HashMap<InetAddress, Chatter>();
	}
	
	public void connect() throws IOException{
		
		gui.message("Connecting to socket...");		
		dhp = new DiffieHellmanProtocol();		
		connect = true;
	}
	
	public void disconnect() throws IOException{
		if(connect == true){
		gui.message("Disconnecting from channel...\n");
		//implement a disconnector to a channel
		//upon connection..
		sh.terminate(); 
		gui.message("Disconnected"); 
		connect = false;
		} else {
			gui.message("You are not connected to this channel");
		}
	}
	
	public void startPrivateChat(){
		gui.message("Starting private chat");	
	}
	
	public static void main(String args[]){
		new nicknameAsk();
	}
	
	public void onDeviceDiscovery(InetAddress device, String identifier, ReliableChannel channel){
		System.out.println("New device discovered");
		chatters.put(device, new Chatter(device, identifier, channel));
	}
	
	public void onDeviceTimeout(InetAddress device){
		chatters.get(device).onDeviceTimeout();
		chatters.remove(device);
	}
	
	public void onReceive(InetAddress device, byte[] bytes){
		chatters.get(device).onReceive(bytes);
	}
	
	public void onMulticastReceive(String identifier, byte[] bytes){
		gui.message(identifier + ": " + new String(bytes));
	}
	
	public void multicastSend(String message){
		network.getMulticastChannel().sendBytes(message.getBytes());
	}
}
