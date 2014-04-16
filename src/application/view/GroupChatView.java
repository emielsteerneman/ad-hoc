package application.view;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import network.NetworkDevice;


public class GroupChatView extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private final JFileChooser fileChooser;
	
	private String identifier;
	private GroupChatViewListener listener;
	
	private JEditorPane textArea;
	private HTMLEditorKit kit;
	private HTMLDocument doc;
	private DefaultCaret caret;
	private JTextField inputTextField;
	private JButton sendButton;
	private JButton fileInputButton;
	
	private class SendTextMessageActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			String message = inputTextField.getText();
			inputTextField.setText("");
			
			if (message.length() > 0) {
				if (listener != null) {
					listener.onGroupMessageSend(message);
				}
				
				addMessage(identifier, message);
			}
		}
		
	}
	
	private class SendFileActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			int returnVal = fileChooser.showOpenDialog(null);

	        if (returnVal == JFileChooser.APPROVE_OPTION) {
	            File file = fileChooser.getSelectedFile();
	            
	            try {
					byte[] data = Files.readAllBytes(Paths.get(file.getPath()));
					
					if (listener != null) {
						listener.onGroupFileSend(data, file.getName());
					}
					
					addMessage(identifier, "\"" + file.getName() + "\" sent");
				} catch (IOException e1) { 
					addMessage(identifier, "failed sending \"" + file.getName() + "\"");
				}
	        }
		}
		
	}
	
	public GroupChatView(String identifier) {
		this.identifier = identifier;
		
		setLayout(new BorderLayout());
		
		fileChooser = new JFileChooser();
		
		textArea = new JEditorPane("text/html", "");
		textArea.setEditable(false);
		kit = new HTMLEditorKit();
		doc = new HTMLDocument();
		textArea.setEditorKit(kit);
		textArea.setDocument(doc);
		caret = (DefaultCaret) textArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		
		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		add(scrollPane, BorderLayout.CENTER);
		
		JPanel inputPanel = new JPanel(new GridBagLayout());
		inputTextField = new JTextField();
		inputTextField.addActionListener(new SendTextMessageActionListener());
		
		sendButton = new JButton("send");
		sendButton.addActionListener(new SendTextMessageActionListener());
		fileInputButton = new JButton("file upload");
		fileInputButton.addActionListener(new SendFileActionListener());
		
		GridBagConstraints c = null;
		
		c = new GridBagConstraints();
		c.weightx = 1.0;		
		c.fill = GridBagConstraints.HORIZONTAL;
		inputPanel.add(inputTextField, c);
		
		c = new GridBagConstraints();
		c.weightx = 0;
		c.fill = GridBagConstraints.NONE;
		inputPanel.add(sendButton, c);
		
		c = new GridBagConstraints();
		c.weightx = 0;
		c.fill = GridBagConstraints.NONE;
		inputPanel.add(fileInputButton, c);
		
		add(inputPanel, BorderLayout.SOUTH);
	}
	
	public void addMessage(String identifier, String message) {
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		
		StringBuffer sb = new StringBuffer();
    	
    	sb.append("[");
    	sb.append(sdf.format(calendar.getTime()));
    	sb.append("] ");
    	
    	sb.append("<font color=\"red\">");
    	sb.append("&lt;");
    	sb.append(identifier);
    	sb.append("&gt;");
    	sb.append("</font> ");
    	
    	sb.append(message);
    	
		try {
			kit.insertHTML(doc, doc.getLength(), sb.toString(), 0, 0, null);
		} catch (BadLocationException | IOException e) { }
	}
	
	public void addDevice(NetworkDevice networkDevice) {
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		
		StringBuffer sb = new StringBuffer();
    	
    	sb.append("<font color=\"green\">");
    	
    	sb.append("[");
    	sb.append(sdf.format(calendar.getTime()));
    	sb.append("] ");
    	
    	sb.append(" *** ");
    	sb.append(networkDevice.getIdentifier());
    	sb.append(" (");
    	sb.append(networkDevice.getAddress());
    	sb.append(") has joined</font>");
    	
    	try {
			kit.insertHTML(doc, doc.getLength(), sb.toString(), 0, 0, null);
		} catch (BadLocationException | IOException e) { }
	}
	
	public void removeDevice(NetworkDevice networkDevice) {
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		
		StringBuffer sb = new StringBuffer();
    	
    	sb.append("<font color=\"red\">");
    	
    	sb.append("[");
    	sb.append(sdf.format(calendar.getTime()));
    	sb.append("] ");
    	
    	sb.append(" *** ");
    	sb.append(networkDevice.getIdentifier());
    	sb.append(" (");
    	sb.append(networkDevice.getAddress());
    	sb.append(") has left</font>");
    	
    	try {
			kit.insertHTML(doc, doc.getLength(), sb.toString(), 0, 0, null);
		} catch (BadLocationException | IOException e) { }
	}
	
}