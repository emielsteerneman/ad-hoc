package socket;

import gui.GUI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import main.ChatApp;

public class SocketHandler extends Thread {
	private DatagramSocket sock;
	private PrintWriter out;
	private BufferedReader in;
	private boolean listen = true;
	private GUI gui;
	private ChatApp ca;

	public SocketHandler(GUI g, ChatApp c) {
		ca = c;
		gui = g;

		listen();

		gui.message("Setup complete");

	}

	public void receive(byte[] s) throws IOException {
		sock.receive(new DatagramPacket(s, 512));
		gui.receive(gui.getUsername() + ": " + s);
		System.out.println("<- " + s);

	}

	public void send(byte[] s1) throws IOException {
		sock.send(new DatagramPacket(s1, 512));
		gui.message(gui.getUsername() + ": " + s1);
		out.println(s1);
	}

	public void listen() {
		
	}

	public void terminate() {

		sock.close();
		System.out.println("FULLY CLOSED");
	}
}
