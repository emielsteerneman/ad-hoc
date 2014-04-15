package main;

import gui.GUI;
import gui.PrivateChatGUI;

import java.io.IOException;

import socket.SocketHandler;
import diffiehellman.DiffieHellmanProtocol;

public class ChatApp {

	private GUI gui;
	private SocketHandler sh;
	private DiffieHellmanProtocol dhp;
	private boolean connect;
	

	public ChatApp(String username) {
		gui = new GUI(username, sh, this);
		sh = new SocketHandler(gui, this);
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
		new PrivateChatGUI("Bas", new SocketHandler(gui, null), this, "Emiel");
		
		
	}
	
}
