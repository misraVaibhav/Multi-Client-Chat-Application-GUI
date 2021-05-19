/**
 * 
 */
package server;


import java.net.*;
import java.io.*;
import java.util.*;

import javax.swing.*;

import objects.Streams;

import java.awt.Dimension;

/**
 * @author vaibhav
 *
 */
public class Server {
	
	 /**
	  * main method of the Main-Class of the jar file
	  */
	public static void main(String[] args) {
		new Server();	
	}
	
	/*
	 * start Listening through any available port, generate GUI to inform the user of server running
	 * and start listening through that port
	 */
	Server() {
		
		clients = new Hashtable<String,Streams>();
		String message = "Using port number 50000\nTo listen to clients through a different port, type the port number:\n";
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {						
				String str = (String)JOptionPane.showInputDialog(frame, message, "50000");
				if(str == null) System.exit(0);			
				str = str.trim();
				pNumber = Integer.parseInt(str);
//				new SwingWorker<Object, Object>() {
//
//					@Override
//					protected Object doInBackground() throws Exception {
						try {
							Server.this.server = new ServerSocket(pNumber);
						} catch(IOException ioe) {
							System.out.println("Could't connect to server: " + ioe.getMessage());
						}
//						return null;
//					}
//
//					@Override
//					protected void done() {	
						if(server == null) {							
							JOptionPane.showMessageDialog(frame, "Port in Use!", "Error!" , JOptionPane.ERROR_MESSAGE);
//							new Server();
							System.exit(0);
						}
//						} else {						
							setGUI();
////						}
////						super.done();
////					}				
////					
////				}.execute();	
//				
//			}
//		});	
			}
		});
		
	}
			

	/*
	 * the following method code runs in Event Dispatch Thread as it conains Swing components 
	 */	
	private void setGUI() {
		JFrame.setDefaultLookAndFeelDecorated(true);
		frame = new JFrame();
		try {
			sLabel = new JTextArea();
			sLabelPane = new JScrollPane();
			sLabelPane.setViewportView(sLabel);
			sLabel.setEditable(false);
			sLabel.setText("Server Listening at: \n" + InetAddress.getLocalHost() + "\nPort: " + pNumber + "\n\nIf client is on the same machine,\nuse localhost as the IP address\nand port as mentioned above,\nelse if on a different machine,\nuse the above IP address and port number.");
		} catch (UnknownHostException uhe) {
			uhe.printStackTrace();
		}
		frame.add(sLabelPane);
		frame.setPreferredSize(new Dimension(255,200));
		frame.setResizable(false);
		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	
		startListening();
	}
	
	/*
	 * start listening to specified port forever
	 */	
	private void startListening() {
		
		new SwingWorker<Object,Object>() {

			@Override
			protected Object doInBackground() throws Exception {
				while(true) {			
					try {
						Socket socket = server.accept();
						new Thread(new ClientHandler(Server.this, socket)).start();
						System.out.println("Client connected at: " + socket.getRemoteSocketAddress());
					} catch(IOException ioe) {
						System.out.println("Error establishing connection: " + ioe.getMessage());
					}	
					
				}	
			}
		}.execute();
		
	}
	
	
	public Hashtable<String,Streams> getClients() {
		return clients;
	}	
	
	public JTextArea getsLabel() {
		return sLabel;
	}
	
	/*
	 * global variables 
	 */
	private ServerSocket server;	
	private JFrame frame;
	private JTextArea sLabel;
	private JScrollPane sLabelPane;
	private int pNumber;	
	
	/* 
	 * hastable to store names and Input/Output streams of all the clients
	 */
	private Hashtable<String,Streams> clients = new Hashtable<String,Streams>();
	
}
