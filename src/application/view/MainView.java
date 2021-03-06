package application.view;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;

import application.ChatClient;
import network.NetworkDevice;


public class MainView extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private DefaultListModel<NetworkDevice> deviceListModel;
	private JList<NetworkDevice> deviceList;
	private JScrollPane scrollPane;
	private JTabbedPane chatsPane;
	private JSplitPane splitPane;
	
	private ChatClient client;
	
	private GroupChatView groupChatTab;
	private HashMap<NetworkDevice, PrivateChatView> privateChatTabs;
	
	private String identifier;
	
	private class DeviceListSelectionListener extends MouseAdapter {
		@Override
	    public void mouseClicked(MouseEvent evt) {
			if (evt.getClickCount() == 2) {
				NetworkDevice device = deviceList.getSelectedValue();
				
				if (!privateChatTabs.containsKey(device)) {
					PrivateChatView privateChatView = new PrivateChatView(identifier, device);
					privateChatView.setPrivateChatViewListener(client);
					
					privateChatTabs.put(device, privateChatView);
					
					chatsPane.addTab(device.toString(), privateChatView);
					chatsPane.setSelectedComponent(privateChatView);
				} else {
					chatsPane.setSelectedComponent(privateChatTabs.get(device));
				}
			}
		}
	}
	
	public MainView(String identifier, ChatClient client) {
		this.identifier = identifier;
		this.client = client;
		
		setLayout(new BorderLayout());
		
		groupChatTab = new GroupChatView(identifier);
		groupChatTab.setGroupChatViewListener(client);
		privateChatTabs = new HashMap<>();
		
		chatsPane = new JTabbedPane();
		chatsPane.addTab("Groupchat", groupChatTab);
		chatsPane.setMinimumSize(new Dimension(400, 0));
		
		deviceListModel = new DefaultListModel<>();
		
		deviceList = new JList<>(deviceListModel);
		deviceList.setCellRenderer(new DefaultListCellRenderer());
		deviceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		deviceList.addMouseListener(new DeviceListSelectionListener());
		
		scrollPane = new JScrollPane(deviceList);
		scrollPane.setMinimumSize(new Dimension(100, 0));
		
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, chatsPane, scrollPane);
		splitPane.setResizeWeight(1.0);
		
		add(splitPane, BorderLayout.CENTER);
	}
	
	public void newPrivateMessage(NetworkDevice networkDevice, String message) {
		if (!privateChatTabs.containsKey(networkDevice)) {
			PrivateChatView privateChatView = new PrivateChatView(identifier, networkDevice);
			privateChatView.setPrivateChatViewListener(client);
			
			privateChatTabs.put(networkDevice, privateChatView);
			
			chatsPane.addTab(networkDevice.toString(), privateChatView);
		}
		
		privateChatTabs.get(networkDevice).addMessage(networkDevice.getIdentifier(), message);
	}
	
	public void newGroupMessage(NetworkDevice networkDevice, String message) {
		groupChatTab.addMessage(networkDevice.getIdentifier(), message);
	}
	
	public void addNetworkDevice(NetworkDevice networkDevice) {
		groupChatTab.addDevice(networkDevice);
		deviceListModel.addElement(networkDevice);
	}
	
	public void removeNetworkDevice(NetworkDevice networkDevice) {
		groupChatTab.removeDevice(networkDevice);
		deviceListModel.removeElement(networkDevice);
		
		if (privateChatTabs.containsKey(networkDevice)) {
			chatsPane.remove(privateChatTabs.get(networkDevice));
			privateChatTabs.remove(networkDevice);
		}
	}
	
}