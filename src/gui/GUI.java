package gui;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import main.ChatApp;
import socket.SocketHandler;
import diffiehellman.DiffieHellmanProtocol;

public class GUI extends JFrame{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final int WIDTH_MIN = 400;
	public static final int HEIGHT_MIN = 400;
	
	public static final int WIDTH = 600;
	public static final int HEIGHT = 600;
	
	public static final int WIDTH_MAX = 800;
	public static final int HEIGHT_MAX = 800;
	
	public static final Color[] COLORS = new Color[]{new Color(60, 60, 60), Color.RED, Color.YELLOW, Color.GREEN, Color.BLUE};	//used by view
	
	
	private JTextArea a;
	private JTextArea b;
	private JPanel mainPanel, buttonPanel, menuPanel, textPanel;
	private JTextArea textArea;
	private JTextField inet, usernameField;
	private static String username;
	private ArrayList<String> connectedPeople;
	private DiffieHellmanProtocol dhp;
	private SocketHandler sh;
	private ChatApp ca;
	private JComboBox<String> cb;
	private String[] sa;
	private String[] COLOR = {"Red", "Blue", "Yellow", "Green"};
	private boolean connect = false;
	private Color backgroundColor = new Color(176,224,230);
	private JFrame frame;

	public GUI(String username, SocketHandler sh, ChatApp ca) {
		this.sh = sh;
		this.ca = ca;
		connectedPeople = new ArrayList<String>();
		connectedPeople.add("Bas");
		connectedPeople.add("Emiel");
		connectedPeople.add("AEde");
		connectedPeople.add("Max");
		
		GUI.username = username;
		
		GridBagConstraints c = new GridBagConstraints();
		Font font = new Font("Arial", Font.BOLD, 20);
	//	backgroundColor = new Color(176,224,230);
		
		mainPanel = new JPanel();
		mainPanel.setLayout(new GridBagLayout());
		mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 1, 10, 1));
		mainPanel.setBackground(backgroundColor);
		
		//buttonPanel
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridBagLayout());
		buttonPanel.setBackground(Color.BLACK);
		
		buttonPanel.setMinimumSize(new Dimension(600, 600));
		buttonPanel.setMaximumSize(new Dimension(600, 600));
		buttonPanel.setPreferredSize(new Dimension(600, 600));
		
		a = new JTextArea();
		a.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		a.setMinimumSize(new Dimension(600,550));
		a.setMaximumSize(new Dimension(600, 550));
		a.setPreferredSize(new Dimension(600, 550));
		a.setBackground(new Color(240,255,255));
		
		a.setEditable(false);
		
		b = new JTextArea();
		b.setBorder(new CompoundBorder(new MatteBorder(3,0,0,0,backgroundColor),(new EmptyBorder(5, 5, 0, 0))));
		b.setMinimumSize(new Dimension(600,30));
		b.setMaximumSize(new Dimension(600, 30));
		b.setPreferredSize(new Dimension(600, 30));
		

		c.gridx = 0;
		c.gridy = 0;
		c.fill = c.BOTH;
		c.weightx = 1;
		c.weighty = 1;
		buttonPanel.add(a,c);
		
		c.gridy = 1;
		c.fill = c.HORIZONTAL;
		c.weightx = 1;
		c.weighty = 0;
		buttonPanel.add(b,c);
		
////////menuPanel
		menuPanel = new JPanel();
		menuPanel.setLayout(new GridLayout(16, 1));
		menuPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
		menuPanel.setBackground(backgroundColor);
		menuPanel.setMinimumSize(new Dimension(250, 100));
		menuPanel.setMaximumSize(new Dimension(250, 100));
		menuPanel.setPreferredSize(new Dimension(250, 100));
		
		JButton btn;
		JLabel lbl;
		
		MatteBorder mborder = new MatteBorder(3,3,3,3,backgroundColor);
	
	
	//title
		lbl = new JLabel("Public Chat");
		lbl.setFont(font);
		menuPanel.add(lbl);
		
	//Username	
		lbl = new JLabel("Username");
		lbl.setFont(font);
		menuPanel.add(lbl);
		
		usernameField = new JTextField();
		usernameField.setEditable(false);
		usernameField.setBorder(mborder);
		usernameField.setFont(font);
		usernameField.setText(username);
		menuPanel.add(usernameField);
	//InetAdress
		lbl = new JLabel("InetAdress");
		lbl.setFont(font);
		menuPanel.add(lbl);
		
		inet = new JTextField();
		inet.setEditable(false);
		inet.setBorder(mborder);
		inet.setFont(font);
		inet.setText("Current InetAddress, needs to be implemented");//InetAddress.getLocalHost().getHostAddress());
		menuPanel.add(inet);
		
	//port
//		lbl = new JLabel("Port");
//		lbl.setFont(font);
//		menuPanel.add(lbl);
//		
//		port = new JTextField();
//		port.setBorder(border);
//		port.setFont(font);
//		menuPanel.add(port);
	
	//Connect
		btn = new JButton("Connect");
		btn.setFont(font);
		menuPanel.add(btn);
		btn.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				try {
					connect();
				} catch (IOException e1) {
					message("Could not connect");
					e1.printStackTrace();
				}
			}

			
		});
	

		
	//Disconnect
		btn = new JButton("Disconnect");
		btn.setFont(font);
		menuPanel.add(btn);
		
		btn.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				try {
					disconnect();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});

	
		
			 
	//Send
		btn = new JButton("Send");
		btn.setFont(font);
		menuPanel.add(btn);
		btn.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				message();
				b.setText("");
			}
		});
////////textPanel
		textPanel = new JPanel();
		textPanel.setLayout(new BorderLayout());
		textPanel.setBorder(mborder);
		textPanel.setBackground(backgroundColor);
		textPanel.setMinimumSize(new Dimension(300, 150));
		textPanel.setMaximumSize(new Dimension(300, 150));
		textPanel.setPreferredSize(new Dimension(300, 150));
		
		textArea = new JTextArea();
		textArea.setAlignmentY(TOP_ALIGNMENT);
		
		font = new Font("Arial", Font.PLAIN, 18);
		textArea.setFont(font);
		textArea.setText(representArrayList(connectedPeople));
		
		JScrollPane sp = new JScrollPane(textArea);
		textPanel.add(sp, BorderLayout.CENTER);
		
		b.addKeyListener(new KeyListener(){

			public void keyPressed(KeyEvent e){
				if(e.getKeyCode() == KeyEvent.VK_ENTER){
					e.consume();
					message();
					b.setText("");
				}
			}
			public void keyTyped(KeyEvent e){};
			public void keyReleased(KeyEvent e){};
		});
		
		
		lbl = new JLabel("Private Chat");
		lbl.setFont(font);
		menuPanel.add(lbl);
		
		
		//combobox
		cb = new JComboBox<String>(ArrayListToStringArray(connectedPeople));
		cb.setSelectedIndex(connectedPeople.size()-1);
		menuPanel.add(cb);
		
		//start channel button
		
		btn = new JButton("Start Private Chat");
		btn.setFont(font);
		btn.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				String chosenOne = (String) cb.getSelectedItem();
				message("Starting new channel with: " + chosenOne);
				startPrivateChat();
				//start new channel with chosenOne
			}
		});
		menuPanel.add(btn);
		
		//filler
		lbl = new JLabel();
		menuPanel.add(lbl);
		
		//Stylist
		cb = new JComboBox<String>(COLOR);
		cb.setSelectedIndex(COLOR.length-1);
		menuPanel.add(cb);
		
		btn = new JButton("Confirm");
		btn.setFont(font);
		btn.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				String chosenStyle = (String) cb.getSelectedItem();
				message("Changing style to: " + chosenStyle);
				updateColors(chosenStyle);
				message("updated");
			}
		});
		menuPanel.add(btn);		

		//ADD ALL TO mainPanel
		c.gridx = 1;
		c.gridy = 0;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 1;
		mainPanel.add(buttonPanel, c);
		
		c.gridx = 2;
		c.weighty = 1;
		c.weightx = 0;
		c.fill = GridBagConstraints.VERTICAL;
		mainPanel.add(textPanel, c);
		
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.VERTICAL;
		c.weighty = 1;
		c.weightx = 0;
		mainPanel.add(menuPanel, c);

		frame = new JFrame("GUI");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		frame.add(mainPanel);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		
		
	}
	
	class IndentedRenderer extends DefaultListCellRenderer
	{

	public Component getListCellRendererComponent(JList list,Object value,
						  int index,boolean isSelected,boolean cellHasFocus)
	  {
		JLabel lbl = (JLabel)super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
		lbl.setBorder(BorderFactory.createEmptyBorder(0,10,0,0));//5 is the indent, modify to suit
		return lbl;
	  }
	}
		
	
	public void message(){
		if(connect == true){
	String newMessage = username +": " + b.getText();
	String finalSendMessage = a.getText() + "\n" +  newMessage;
	a.setText(finalSendMessage);
	message(newMessage);
		} else {
			message("You are not connected to this channel.");
		}
		//Connect to other sockets, send message
	
	
	}
	
	//system send
	public void message(String s){
		String finalField = a.getText() + "\n" + s;
		a.setText(finalField);
		}
	
	public void connect() throws UnknownHostException, IOException{
		message("Connecting to channel with username " + username + "...\n");
		ca.connect();
		//implement a connector to a channel
		//upon connection..
		
		
	}
	
	public void startPrivateChat(){
			ca.startPrivateChat();
			
	}
	
	public void disconnect() throws IOException{
		
		ca.disconnect();

		//close channel

		
	}
	
	public void send(String s) throws IOException{
		
		byte[] s1 = dhp.encrypt(s);
		sh.send(s1);
		
	}
	
	public void receive(String s){
		//String a = dhp.decrypt(s);
		message(s);
		
	}
	
	public String representArrayList(ArrayList<String> al){
		String a = new String();
		for(int i =0; i < al.size(); i++){
			a = a + al.get(i) + "\n";
		}
		return a;
	}
	
	public String[] ArrayListToStringArray(ArrayList<String> al){
		sa = new String[al.size()];
		for(int i =0; i < al.size(); i++){
			sa[i] = al.get(i);
		}
		return sa;
	}
	
	public void checkConnections(){
		connectedPeople.add("gevonden connectie");
	}
	
	public String getUsername(){
		return username;
	}
	
	public void updateColors(String chosenColor){
		message("called");
		if(chosenColor == "Red"){
			backgroundColor = Color.RED;
			message("red");
		} else if(chosenColor == "Blue"){
			backgroundColor = Color.BLUE;
			message("blu");
		} else if(chosenColor == "Yellow"){
			backgroundColor = Color.YELLOW;
			message("ylw");
		} else if(chosenColor == "Green"){
			backgroundColor = Color.GREEN;
			message("grn");
		}
		frame.repaint();
		frame.revalidate();
		
	}
	public static void main(String[] args){
			
	}
	
	
	
}