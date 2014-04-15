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
import java.util.Arrays;

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


public class ChatView extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private final JFileChooser fileChooser;
	
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
				addMessage("adsfasdf", message);
			}
		}
		
	}
	
	private class SendFileActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			
			
			int returnVal = fileChooser.showOpenDialog(null);

	        if (returnVal == JFileChooser.APPROVE_OPTION) {
	            File file = fileChooser.getSelectedFile();
	            
	            byte[] data = new byte[0];
	            
	            try {
					data = Files.readAllBytes(Paths.get(file.getPath()));
				} catch (IOException e1) { }
	            
	            if (data.length > 0) {
	            	System.out.println(Arrays.toString(data));
	            	
	            	addMessage("yolo", new String(data));
	            }
	        }
		}
		
	}
	
	public ChatView() {
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
		try {
			kit.insertHTML(doc, doc.getLength(), "<b>" + identifier + "</b>: " + message, 0, 0, null);
		} catch (BadLocationException | IOException e) { }
	}
	
	public JTextField getInputTextField() {
		return inputTextField;
	}
	
	public JButton getSendButton() {
		return sendButton;
	}
	
	public JButton getFileInputButton() {
		return fileInputButton;
	}
	
}