package application.view;
import java.awt.BorderLayout;
import java.awt.Component;
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
	
	private ChatView groupChatTab;
	private HashMap<NetworkDevice, ChatView> privateChatTabs;
	
	private class DeviceListSelectionListener extends MouseAdapter {
		@Override
	    public void mouseClicked(MouseEvent evt) {
			if (evt.getClickCount() == 2) {
				NetworkDevice device = deviceList.getSelectedValue();
				
				if (!privateChatTabs.containsKey(device)) {
					ChatView privateChatView = new ChatView();
					
					privateChatTabs.put(deviceList.getSelectedValue(), new ChatView());
					
					chatsPane.addTab(device.toString(), privateChatView);
				}
			}
		}
	}
	
	public MainView() {
		setLayout(new BorderLayout());
		
		groupChatTab = new ChatView();
		privateChatTabs = new HashMap<>();
		
		chatsPane = new JTabbedPane();
		chatsPane.addTab("Groupchat", groupChatTab);
		chatsPane.setMinimumSize(new Dimension(400, 0));
		
		deviceListModel = new DefaultListModel<>();
		
		deviceList = new JList<>(deviceListModel);
		deviceList.setCellRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = 1L;

			public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
				
                setText(((NetworkDevice) value).getIdentifier());
                
                return this;
            }
        });
		deviceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		deviceList.addMouseListener(new DeviceListSelectionListener());
		
		scrollPane = new JScrollPane(deviceList);
		scrollPane.setMinimumSize(new Dimension(100, 0));
		
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, chatsPane, scrollPane);
		splitPane.setResizeWeight(1.0);
		splitPane.setDividerLocation(splitPane.getSize().width
                - splitPane.getInsets().right
                - splitPane.getDividerSize()
                - 200);
		
		add(splitPane, BorderLayout.CENTER);
	}
	
	public ChatView getGroupChatTab() {
		return groupChatTab;
	}
	
	public HashMap<NetworkDevice, ChatView> getPrivateChatTabs() {
		return privateChatTabs;
	}
	
}