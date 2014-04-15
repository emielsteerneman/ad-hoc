package application;

import javax.swing.JFrame;

public class Program extends JFrame {
	private static final long serialVersionUID = 1L;
	
	private ChatView chatView;
	
	public Program() {
		super("Chat");
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		chatView = new ChatView();
		add(chatView);
		
		setVisible(true);
	}
	
	public static void main(String[] args) {
		new Program();
	}
	
}
