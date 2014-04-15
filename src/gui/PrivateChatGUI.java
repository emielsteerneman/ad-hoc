package gui;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import network.Protocol;
import main.ChatApp;
import main.Chatter;
import socket.SocketHandler;
import diffiehellman.DiffieHellmanProtocol;
public class PrivateChatGUI extends JFrame{
	
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
	private JPanel mainPanel, buttonPanel, textPanel;
	private JTextArea textArea;
	private String username;
	private ArrayList<String> connectedPeople;
	private DiffieHellmanProtocol dhp;
	private static SocketHandler sh;
	private static ChatApp ca;
	private String[] sa;
	private Color backgroundColor = new Color(176,224,230);
	private JFrame frame;
	
	private Chatter chatter;
	
	public PrivateChatGUI(String otherUser, Chatter chatter) {
		//connect to other user
		connectedPeople = new ArrayList<String>();
		connectedPeople.add(otherUser);
		
		this.chatter = chatter;
		
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
		
//		buttonPanel.setMinimumSize(new Dimension(600, 600));
//		buttonPanel.setMaximumSize(new Dimension(600, 600));
//		buttonPanel.setPreferredSize(new Dimension(600, 600));
		
		a = new JTextArea();
		a.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		a.setMinimumSize(new Dimension(600,550));
		a.setMaximumSize(new Dimension(600, 550));
		a.setPreferredSize(new Dimension(600, 550));
		a.setBackground(new Color(240,255,255));
		
		JScrollPane scrollp = new JScrollPane(a);
		scrollp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollp.setPreferredSize(new Dimension(600,500));
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
		buttonPanel.add(scrollp,c);
		
		c.gridy = 1;
		c.fill = c.HORIZONTAL;
		c.weightx = 1;
		c.weighty = 0;
		buttonPanel.add(b,c);
		
////////menuPanel
		
		
		JButton btn;
		JLabel lbl;
		
		MatteBorder mborder = new MatteBorder(3,3,3,3,backgroundColor);
	
	
	
	
////////textPanel
		textPanel = new JPanel();
		textPanel.setLayout(new BorderLayout());
		textPanel.setBorder(mborder);
		textPanel.setBackground(backgroundColor);
		textPanel.setMinimumSize(new Dimension(300, 50));
		textPanel.setMaximumSize(new Dimension(300, 50));
		textPanel.setPreferredSize(new Dimension(300, 50));
		
		textArea = new JTextArea();
		textArea.setAlignmentY(TOP_ALIGNMENT);
		
		font = new Font("Arial", Font.PLAIN, 18);
		textArea.setFont(font);
		textArea.setText(representArrayList(connectedPeople));
		
		JScrollPane sp = new JScrollPane(textArea);
		sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		textPanel.add(sp, BorderLayout.CENTER);
		
		b.addKeyListener(new KeyListener(){
			public void keyPressed(KeyEvent e){
				if(e.getKeyCode() == KeyEvent.VK_ENTER){
					e.consume();
					message(username + ": " + b.getText());
					b.setText("");
				}
			}
			public void keyTyped(KeyEvent e){};
			public void keyReleased(KeyEvent e){};
		});
		
		
		//ADD ALL TO mainPanel
		c.gridx = 0;
		c.gridy = 1;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 1;
		mainPanel.add(buttonPanel, c);
		
		c.gridx = 0;
		c.gridy = 0;
		c.weighty = 1;
		c.weightx = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		mainPanel.add(textPanel, c);
		frame = new JFrame("GUI");
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
		
	public void message(String s){
		chatter.send(Protocol.MESSAGE, s);
		a.append("\n" + s);
	}
	
	public void connect(String otherPerson) throws UnknownHostException, IOException{
		message("Connecting to channel with username " + username + "...\n");
		ca.connect();
		//connect to the otha
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
	
}