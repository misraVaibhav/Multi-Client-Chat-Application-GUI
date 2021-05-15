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
		try {
			new Server(new ServerSocket(0));
		} catch (IOException ioe) {
			System.out.println("Error in finding port to listen through: " + ioe.getMessage());
		}
	}
	
	/*
	 * start Listening through any available port, generate GUI to inform the user of server running
	 * and start listening through that port
	 */
	Server(ServerSocket server) {
		this.server = server;
		try {			
			
			clients = new Hashtable<String,Streams>();
			
			storePort();			
			
			setGUI();
					
			startListening();
			
		} catch(IOException ioe) {
			System.out.println("Error during listening for clients: " + ioe.getMessage());
		}
		
	}
	
	/*
	 * store port that is listening to clients in port.txt to inform clients of the port they need to connect to
	 */
	private void storePort() throws FileNotFoundException, IOException {
		File fl = new File("port.txt");
		FileOutputStream f = new FileOutputStream(fl,false);
		f.write(String.valueOf(server.getLocalPort()).getBytes());
		f.close();
	}
	
	/*
	 * the following method code runs in Event Dispatch Thread as it conains Swing components 
	 */	
	private void setGUI() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JFrame.setDefaultLookAndFeelDecorated(true);
				frame = new JFrame();
				frame.add(new JLabel("Server Running..."));
				frame.setPreferredSize(new Dimension(130,80));
				frame.setResizable(false);
				frame.pack();
				frame.setVisible(true);
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			}
		});	
	}
	
	/*
	 * start listening to specified port forever
	 */	
	private void startListening() throws IOException {
		while(true) {			
			Socket socket = server.accept();
			
			// after accepting a client, its respective client handler starts in a new thread
			new Thread(new ClientHandler(socket)).start();
			System.out.println("Client connected at: " + socket.getRemoteSocketAddress());
		}
	}
	
	
	public static Hashtable<String,Streams> getClients() {
		return Server.clients;
	}	
	
	/*
	 * global variables 
	 */
	private ServerSocket server;	
	JFrame frame;
	
	/* 
	 * hastable to store names and Input/Output streams of all the clients
	 */
	private static Hashtable<String,Streams> clients = new Hashtable<String,Streams>();
	
}