/**
 * 
 */
package client;

import java.net.*;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import objects.Message;
import objects.MessageType;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

/**
 * @author vaibhav
 *
 */
public class Client {

	/*
	 * main method of the Main-Class of the Client.jar file
	 */
	public static void main(String[] args) throws IOException {
		new Client();
	}
	
	/*
	 * connect to the server, store input/output streams and setup GUI for client interaction 
	 */
	Client() {		
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					startConnection();
					startChatGUI();
				} catch(IOException ioe) {
					System.out.println("Could't connect to server: " + ioe.getMessage());
				}
			}
		});	
	}

	/*
	 * connect to the server through the port saved by the server in port.txt
	 */
	private void startConnection() throws UnknownHostException, IOException {
		System.out.println("Connecting to server...");
		FileReader fr = new FileReader(new File("port.txt"));
		BufferedReader br = new BufferedReader(fr);
		socket = new Socket("localhost",Integer.parseInt(br.readLine()));
		fr.close();
		System.out.println("Connected");
	}
	
	/*
	 * generate GUI for user interaction
	 */
	private void startChatGUI() throws IOException {	
		
		/*
		 * showing input dialog box asking client to enter their name and sending it to server
		 */
		JFrame.setDefaultLookAndFeelDecorated(true);
		frame = new JFrame();
		String str = "";
		String message = "Enter your name to enter the chat:";
		while(str.isEmpty()) {
			str = (String)JOptionPane.showInputDialog(frame, message);
			if(str == null) System.exit(0);
			message = "Name cannot be empty!\nEnter your name to enter the chat:";
		}
		this.name = str;
		this.out = new ObjectOutputStream(socket.getOutputStream());
		this.in = new ObjectInputStream(socket.getInputStream());
		out.writeObject(new Message(str, MessageType.SEND_NAME));
		out.flush();
		
		/*
		 * set global chatting GUI
		 */
		frame.setTitle(str);
		splitPane = new JSplitPane();
		leftPanel = new JPanel();
		rightPanel = new JPanel();
		globalChat = new JScrollPane();
		globalChatArea = new JTextArea();
		globalChatArea.setEditable(false);
		globalChatArea.setFont(globalChatArea.getFont().deriveFont(18f));
		globalChatArea.setBackground(new Color(135, 161, 204));
		messageArea = new JScrollPane();
		messageText = new JTextArea();
		sendButton = new JButton("Send");
		sendButton.setMargin(new Insets(1,1,1,1));
		
		/*
		 * send the message to all the clients
		 */
		sendButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(!messageText.getText().equals("")) {
					try {
						out.writeObject(new Message(messageText.getText(),MessageType.CLIENT_GLOBAL_MESSAGE));
						out.flush();
					} catch(IOException ioe) {
						System.out.println("Error establishing connection: " + ioe.getMessage());
					}			
					messageText.setText("");			
				} 		
			}
		});
		globalChat.setViewportView(globalChatArea);
		messageArea.setViewportView(messageText);
		leftPanel.setLayout(new GridBagLayout());
		leftPanel.setBorder(BorderFactory.createTitledBorder("Global Chat"));
		leftPanel.setFont(leftPanel.getFont().deriveFont(18f));

		
		GridBagConstraints gbc = new GridBagConstraints();
		Insets insets = new Insets(5,5,5,5);
		
		addComponent(leftPanel,globalChat, gbc, GridBagConstraints.BOTH, GridBagConstraints.CENTER, 1, 1, 0, 1, 2, 1, insets);
		addComponent(leftPanel,messageArea, gbc, GridBagConstraints.BOTH, GridBagConstraints.CENTER, 1, 0, 0, 2, 1, 1, insets);
		addComponent(leftPanel,sendButton, gbc, GridBagConstraints.NONE, GridBagConstraints.CENTER, 0, 0, 1, 2, 1, 1, insets);
		
		splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setDividerLocation(300);
		splitPane.setLeftComponent(leftPanel);
		splitPane.setRightComponent(rightPanel);
		
		frame.setPreferredSize(new Dimension(600,400));
		frame.setLayout(new GridLayout());
		frame.add(splitPane);
		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		/*
		 * start a helper thread that keeps on reading messages from server and also sets up private chatting GUI
		 */
		new Thread(new ClientHelper(this)).start();
		
		/*
		 * request for client list from server initially to show online clients as the client joins chat
		 */
		out.writeObject(new Message());
		out.flush();
	}
	
	/*
	 * helper method for adding gridbaglayout constraints
	 */
	private void addComponent(Container parent, Component child, GridBagConstraints gbc, int fill, int anchor, double weightx, double weighty, int gridx, int gridy, int gridwidth, int gridheight, Insets insets) {
		gbc.fill = fill;
		gbc.anchor = anchor;
		gbc.weightx = weightx;
		gbc.weighty = weighty;
		gbc.gridx = gridx;
		gbc.gridy = gridy;
		gbc.gridwidth = gridwidth;
		gbc.gridheight = gridheight;
		gbc.insets = insets;
		parent.add(child, gbc);
	}
	
	/*
	 * getters
	 */
	public ObjectOutputStream getOut() {
		return out;
	}

	public String getName() {
		return name;
	}

	public JPanel getRightPanel() {
		return rightPanel;
	}

	public ObjectInputStream getIn() {
		return in;
	}

	public JTextArea getGlobalChatArea() {
		return globalChatArea;
	}
	
	public JFrame getFrame() {
		return frame;
	}

	/*
	 * global variables
	 */
	private Socket socket;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private String name;
	
	private JFrame frame;
	private JSplitPane splitPane;
	private JPanel leftPanel;
	private JPanel rightPanel;
	private JScrollPane globalChat;
	private JTextArea globalChatArea;
	private JScrollPane messageArea;
	private JTextArea messageText;
	private JButton sendButton;
//	private JLabel gLabel;
}