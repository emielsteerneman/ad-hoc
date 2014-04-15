package application;

import java.awt.GridBagLayout;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import network.NetworkDevice;

public class ChatView extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private DefaultListModel<NetworkDevice> listModel;
	private JList<NetworkDevice> deviceList;
	JScrollPane scrollPane;
	
	public ChatView() {
		listModel = new DefaultListModel<>();
		deviceList = new JList<>(listModel);
		scrollPane = new JScrollPane();
		
		setLayout(new GridBagLayout());
		
		scrollPane.setViewportView(deviceList);
		add(scrollPane);
		
		listModel.addElement(new NetworkDevice(null, "adsfadsfasdf"));
	}
	
	
	
}
