package gui;



import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import main.ChatApp;

public class nicknameAsk {
		
	private JFrame frame;
	private JPanel main;	
	private JTextArea username;
	private JButton confirm;
	private JLabel msg;
	
	public nicknameAsk(){
		Font font = new Font("Arial", Font.BOLD, 20);
		
		frame = new JFrame("Rolit");
		frame.setPreferredSize(new Dimension(300, 300));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		
		main = new JPanel();
		main.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
		main.setLayout(new GridLayout(4 , 1, 0, 20));
		
		JLabel lbl = new JLabel("Nickname", JLabel.CENTER);
		lbl.setFont(font);
		main.add(lbl);
		
		username = new JTextArea("");
		username.setFont(font);
	
		username.addKeyListener(new KeyListener(){

			public void keyPressed(KeyEvent e){
				if(e.getKeyCode() == KeyEvent.VK_ENTER){
					e.consume();
					confirmUsername();
				}
			}
			public void keyTyped(KeyEvent e){};
			public void keyReleased(KeyEvent e){};
		});
		main.add(username);
		
		confirm = new JButton("Confirm");
		confirm.setFont(font);
		confirm.setFocusable(false);
		main.add(confirm);
		
		msg = new JLabel("", JLabel.CENTER);
		msg.setFont(font);
		main.add(msg);
		
		confirm.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				confirmUsername();
			}
		});
		
		frame.add(main);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	
	public void confirmUsername(){
		String s = username.getText();
		if(s.equals("")){
			msg.setText("Please enter a username");
		}else if(s.length() > 20){
			msg.setText("Username is too long");
		}else if(s.length() < 2){
			msg.setText("Username is too short");
		}else{
			//new GUI(s);
			new ChatApp(s);
			frame.dispose();
		}
		
	
		
	}
	
	public static void main(String[] argv){
		new nicknameAsk();
			}
}
