package main;

import java.io.IOException;

import diffiehellman.DiffieHellmanProtocol;
import gui.GUI;
import socket.SocketHandler;

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
	
}
