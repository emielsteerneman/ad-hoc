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

import network.NetworkDevice;


public class MainView extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private DefaultListModel<NetworkDevice> deviceListModel;
	private JList<NetworkDevice> deviceList;
	private JScrollPane scrollPane;
	private JTabbedPane chatsPane;
	private JSplitPane splitPane;
	
	private PrivateChatView groupChatTab;
	private HashMap<NetworkDevice, PrivateChatView> privateChatTabs;
	
	private String identifier;
	
	private class DeviceListSelectionListener extends MouseAdapter {
		@Override
	    public void mouseClicked(MouseEvent evt) {
			if (evt.getClickCount() == 2) {
				NetworkDevice device = deviceList.getSelectedValue();
				
				if (!privateChatTabs.containsKey(device)) {
					PrivateChatView privateChatView = new PrivateChatView(identifier, device);
					
					privateChatTabs.put(deviceList.getSelectedValue(), new PrivateChatView(identifier, device));
					
					chatsPane.addTab(device.toString(), privateChatView);
				}
			}
		}
	}
	
	public MainView(String identifier) {
		this.identifier = identifier;
		
		setLayout(new BorderLayout());
		
		groupChatTab = new PrivateChatView(identifier, null);
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
	
	public PrivateChatView getGroupChatTab() {
		return groupChatTab;
	}
	
	public HashMap<NetworkDevice, PrivateChatView> getPrivateChatTabs() {
		return privateChatTabs;
	}
	
	public void addNetworkDevice(NetworkDevice networkDevice) {
		deviceListModel.addElement(networkDevice);
	}
	
	public void removeNetworkDevice(NetworkDevice networkDevice) {
		deviceListModel.removeElement(networkDevice);
	}
	
}