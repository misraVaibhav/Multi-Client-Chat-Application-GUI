/**
 * 
 */
package server;

import java.net.*;
import java.util.ArrayList;

import objects.Message;
import objects.MessageType;
import objects.Streams;

import java.io.*;

/**
 * @author vaibhav
 *
 */
public class ClientHandler implements Runnable {

	/*
	 * Constructor : store the input/output streams of the corresponding client's socket 
	 */
	ClientHandler(Socket socket) {
		try {
			this.out = new ObjectOutputStream(socket.getOutputStream());
			this.in = new ObjectInputStream(socket.getInputStream());
		} catch (SocketException se) {
			System.out.println("Error establishing connection: " + se.getMessage());
		} catch (IOException ioe) {
			System.out.println("Error establishing connection: " + ioe.getMessage());
		}
	}

	/*
	 * the following method runs after the constructor's initialisation owing to the call to Thread's start method
	 */
	public void run() {
		try {
			
			 /*
			  * get client's name
			  */
			Message message = (Message)in.readObject();
			String str = message.getMessage();
			name = str;
				
			
			/*
			 * store client's name
			 */
			Server.getClients().put(name,new Streams(in,out));
			System.out.println(str + " joined!");
			
			/*
			 * notify all the clients of new client coming online
			 */
			Server.getClients().forEach((k,v) -> {
				if(!k.equals(name)) {
					try {
						v.getOS().writeObject(new Message(new ArrayList<String>(Server.getClients().keySet())));
						v.getOS().flush();
					} catch (IOException ioe) {
						System.out.println("Error establishing connection: " + ioe.getMessage());
					}
				}
			});
			
			/*
			 * start reading for any type of message client sends to server and execute corresponding execution
			 */
			while(true) {
				
				/*
				 * read the message
				 */
				message = (Message)in.readObject();
				final Message msg = message;
			
				if(message.getmType() == MessageType.REQUEST_CLIENT_LIST) {
					/*
					 * when client requests for list of online clients
					 */
					out.writeObject(new Message(new ArrayList<String>(Server.getClients().keySet())));
					out.flush();
				} else if(msg.getmType() == MessageType.CLIENT_GLOBAL_MESSAGE) {
					/*
					 * when client sends a global message
					 */
					Server.getClients().forEach((k,v) -> {						
						try {
							v.getOS().writeObject(new Message(msg.getMessage(), this.name, MessageType.SERVER_GLOBAL_MESSAGE));
							v.getOS().flush();
						} catch (IOException ioe) {
							System.out.println("Error establishing connection: " + ioe.getMessage());
						}
					});
				} else if(message.getmType() == MessageType.CLIENT_PRIVATE_MESSAGE) {
					/*
					 * when client sends a private message
					 */
					ObjectOutputStream out_ = Server.getClients().get(message.getPerson()).getOS(); 
					out_.writeObject(new Message(message.getMessage(), this.name, MessageType.SERVER_PRIVATE_MESSAGE));
					out_.flush();
				}
				
			}
		} catch (ClassNotFoundException cnfe) {
			System.out.println("Error establishing connection: " + cnfe.getMessage());
		} catch (SocketException se) {
			System.out.println(this.name + " left!");
			Server.getClients().remove(name);
			Server.getClients().forEach((k,v) -> {
				try {
					v.getOS().writeObject(new Message(name, MessageType.SEND_CLIENT_LIST_LEFT) );
					v.getOS().flush();
				} catch (IOException ioe) {
					System.out.println("Error establishing connection: " + ioe.getMessage());
				}
			});
		} catch (IOException ioe) {
			System.out.println("Error establishing connection: " + ioe.getMessage());
		} 
	}
	
	/*
	 * global variables 
	 */
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private String name;
}