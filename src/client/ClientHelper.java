/**
 * 
 */
package client;

import java.net.*;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
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
public class ClientHelper implements Runnable{

	/*
	 * define the private chat GUI components
	 */
	ClientHelper(Client Gui) {
		chatWith = null;
		privateMessage = new JTextArea();
		privateChatAreaPane = new JScrollPane();
		privateMessagePane = new JScrollPane();
		privateMessagePane.setViewportView(privateMessage);		
		chats = new Hashtable<String,String>();
		this.Gui = Gui;
	}
	
	/*
	 * the following method runs after the constructor's initialisation owing to the call to Thread's start method
	 */
	public void run() {
		try {
			
			/*
			 * get all online users from server, asked before starting this thread
			 */
			Message message = (Message)Gui.getIn().readObject();
			for(String name : message.getClients()) {
				if(!name.equals(Gui.getName()))
					chats.put(name, "");
			}
			
			/*
			 * set GUI for showing lists of online users the Event Dispatch Thread
			 */
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					setChatsGUI();
				}
			});
			
			/*
			 * keep listening for messages form server
			 */
			while(true) {
				message = (Message)Gui.getIn().readObject();				
				
				if(message.getmType() == MessageType.SEND_CLIENT_LIST) {
					/*
					 * new client list is sent by server when any new client joins
					 */
					for(String name : message.getClients()) {
						if(!chats.containsKey(name) && !name.equals(Gui.getName())) {
							chats.put(name, "");
						}
					}
					
					
					/*
					 * if current private chat GUI is of type chat list, update the users
					 */
					if(chatWith == null) {
						setChatsGUI();
					}
				} else if(message.getmType() == MessageType.SEND_CLIENT_LIST_LEFT) {
					if(chats.get(message.getMessage()).equals(""))
						chats.remove(message.getMessage());
					if(chatWith == null) {
						setChatsGUI();
					}
				} else if(message.getmType() == MessageType.SERVER_PRIVATE_MESSAGE) {
					/*
					 * server sends a private message from one of the other clients
					 */
					if(chatWith != null && chatWith == message.getPerson()) {
						/*
						 * if currently chatting with the corresponding client only update the chat
						 */
						final Message msg = message;
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								if(privateChatArea.getText().equals(""))
									privateChatArea.setText(msg.getPerson() + " - " + "\n    " + msg.getMessage() + "\n\n");
								else									
									privateChatArea.setText(privateChatArea.getText() + msg.getPerson() + " - " + "\n    " +  msg.getMessage() + "\n");
							}
						});
						
					} else {
						/*
						 * if not currently chatting, store in the chats hashtable
						 */
						String pChat = chats.get(message.getPerson());
						chats.put(message.getPerson(),pChat + message.getPerson() + " - " + "\n    " + message.getMessage() + "\n\n");
						if(chatWith == null) {
							setChatsGUI();
						}
					}
				} else if(message.getmType() == MessageType.SERVER_GLOBAL_MESSAGE) {
					/*
					 * server sends a global message from one of the clients
					 */
					final Message msg = message;
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							if((Gui.getGlobalChatArea().getText().equals("")))
									Gui.getGlobalChatArea().setText(msg.getPerson() + " - " + "\n    " + msg.getMessage() + "\n\n");
							else
								Gui.getGlobalChatArea().setText(Gui.getGlobalChatArea().getText() + msg.getPerson() + " - " + "\n    " + msg.getMessage() + "\n\n");
						}
					});					
				} else {
					System.out.println("Error Occured");
				}
			}
		} catch (SocketException se) {
			System.out.println("Error establishing connection: " + se.getMessage());
		} catch (IOException ioe) {
			System.out.println("Error establishing connection: " + ioe.getMessage());
		} catch(ClassNotFoundException cnfe) {
			System.out.println("Error establishing connection: " + cnfe.getMessage());
		}	
	}
	
	/*
	 * update GUI when a user joins for the first time or when back button is pressed from a private chat
	 */
	private void setChatsGUI() {
				
		Gui.getRightPanel().removeAll();
		Gui.getRightPanel().setLayout(new GridLayout());		
		Gui.getRightPanel().setBorder(BorderFactory.createTitledBorder("Online Users"));
		JPanel tPanel = new JPanel();
		
		if(!chats.isEmpty()) {
			tPanel.setLayout(new GridBagLayout());
			
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridwidth = GridBagConstraints.REMAINDER;
            gbc.weightx = 1;
            gbc.weighty = 1;
            tPanel.add(new JPanel(), gbc);
			
			Set<Map.Entry<String, String>> entries = chats.entrySet();		
			for(Map.Entry<String, String> entry : entries ){  
				JLabel chat = new JLabel(entry.getKey());
				JButton open = new JButton(entry.getValue().equals("")?"Start Chat":"Enter Chat");
				open.setMargin(new Insets(1,1,1,1));
				open.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						/*
						 * load a private chat
						 */
						setPrivateChatGUI(entry.getKey(),entry.getValue());
					}	
				});			
				JPanel tPanel_ = new JPanel();
				tPanel_.setLayout(new GridBagLayout());	
				addComponent(tPanel_, chat, gbc, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH, 1, 0, 0, 0, 1, 1, new Insets(5,5,5,5));
				addComponent(tPanel_, open, gbc, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH, 0, 0, 1, 0, 1, 1, new Insets(5,5,5,5));
				tPanel.add(Box.createRigidArea(new Dimension(0,5)));
				gbc = new GridBagConstraints();
	            gbc.gridwidth = GridBagConstraints.REMAINDER;
	            gbc.weightx = 1;
	            gbc.fill = GridBagConstraints.HORIZONTAL;
	            tPanel.add(tPanel_, gbc, 0);

			}
		}
		JScrollPane tPane = new JScrollPane();
		tPane.setViewportView(tPanel);
		
		Gui.getRightPanel().add(tPane);		
		Gui.getRightPanel().revalidate();
		Gui.getRightPanel().repaint();

		
	}
	
	/*
	 * when a users clicks on one of the private chats
	 */
	private void setPrivateChatGUI(String name, String chat) {
		
		Gui.getRightPanel().removeAll();		
		Gui.getRightPanel().setLayout(new GridBagLayout());	
		Gui.getRightPanel().setBorder(BorderFactory.createTitledBorder("Private Chat"));

		chatWith = name;
//		chatWith.setHorizontalAlignment(JLabel.RIGHT);
		privateChatArea = new JTextArea();
		privateChatArea.setEditable(false);
		privateChatArea.setFont(privateChatArea.getFont().deriveFont(18f));
		privateChatArea.setBackground(new Color(135, 161, 204));
		privateChatArea.setText(chat);
		privateChatAreaPane.setViewportView(privateChatArea);
		final String chatName = name;

		backButton = new JButton("Back");
		backButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {	
				if(!privateChatArea.getText().equals("")) {
					chats.replace(chatName, privateChatArea.getText());
				}
				chatWith = null;
				setChatsGUI();
			}
		});
		
		sendButton = new JButton("Send");
		sendButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(!privateMessage.getText().equals("")) {
					if(privateChatArea.getText().equals(""))
						privateChatArea.setText(Gui.getName() + " - " + "\n    " + privateMessage.getText() + "\n");
					else									
						privateChatArea.setText(privateChatArea.getText() + Gui.getName() + " - " + "\n    " + privateMessage.getText() + "\n");
					try {
						Gui.getOut().writeObject(new Message(privateMessage.getText(),name));
						Gui.getOut().flush();
					} catch (IOException ioe) {
						System.out.println("Error establishing connection: " + ioe.getMessage());
					}					
				}
				privateMessage.setText("");
			}
		});
		
		sendButton.setMargin(new Insets(1,1,1,1));
		backButton.setMargin(new Insets(1,1,1,1));
		GridBagConstraints gbc = new GridBagConstraints();
		JPanel extraPanel = new JPanel();
		extraPanel.setLayout(new GridBagLayout());
		Insets insets = new Insets(5,5,5,5);
		JPanel panelB = new JPanel();
		panelB.setLayout(new GridBagLayout());
		panelB.setBorder(BorderFactory.createTitledBorder(name));

		
		addComponent(Gui.getRightPanel(),backButton, gbc, GridBagConstraints.NONE, GridBagConstraints.LINE_START, 1, 0, 0, 0, 1, 1, insets);
		addComponent(panelB,privateChatAreaPane, gbc, GridBagConstraints.BOTH, GridBagConstraints.CENTER, 1, 1, 0, 0, 2, 1, insets);
		addComponent(panelB,privateMessagePane, gbc, GridBagConstraints.BOTH, GridBagConstraints.CENTER, 1, 0, 0, 1, 1, 1, insets);
		addComponent(panelB,sendButton, gbc, GridBagConstraints.NONE, GridBagConstraints.CENTER, 0, 0, 1, 1, 1, 1, insets);
		addComponent(Gui.getRightPanel(),panelB, gbc, GridBagConstraints.BOTH, GridBagConstraints.CENTER, 1, 1, 0, 1, 1, 1, insets);
		Gui.getRightPanel().revalidate();	
		Gui.getRightPanel().repaint();
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
	 * global variables
	 */
	private Client Gui;
	private Hashtable<String,String> chats;
	
	private JButton backButton;
	private String chatWith;
	private JTextArea privateChatArea;
	private JTextArea privateMessage;
	private JButton sendButton;
	private JScrollPane privateChatAreaPane;
	private JScrollPane privateMessagePane;
}