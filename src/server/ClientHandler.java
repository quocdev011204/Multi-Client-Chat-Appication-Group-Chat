/**
 *
 */
package server;

import java.net.*;
import java.util.ArrayList;

import objects.Group;
import objects.Message;
import objects.MessageType;
import objects.Streams;
import objects.GroupChat;

import java.io.*;


public class ClientHandler implements Runnable {

	/*
	 * Constructor : store the input/output streams of the corresponding client's socket
	 */
	ClientHandler(Server server, Socket socket) {
		try {
			this.server = server;
			// Kết nối đến client
			this.socket = socket;
			this.out = new ObjectOutputStream(socket.getOutputStream());
			out.flush(); // đảm bảo là dữ liệu được gửi ngay lập tức
			this.in = new ObjectInputStream(socket.getInputStream());
			// đầu ra và đầu vô để gửi và nhận tin nhắn từ client
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
		System.out.println("in thread");
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
			server.getClients().put(name,new Streams(in,out));
            System.out.println(name + " at " + socket.getInetAddress().getHostAddress()+" joined the Chat!");
            server.getsLabel().setText(server.getsLabel().getText() + "\n\n" + name + " at " + socket.getRemoteSocketAddress()+" joined the Chat!");

			/*
			 * notify all the clients of new client coming online
			 */
			server.getClients().forEach((k,v) -> {
				if(!k.equals(name)) {
					try {
						v.getOS().writeObject(new Message(new ArrayList<String>(server.getClients().keySet())));
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
					out.writeObject(new Message(new ArrayList<String>(server.getClients().keySet())));
					out.flush();
				} else if(msg.getmType() == MessageType.CLIENT_GLOBAL_MESSAGE) {
					/*
					 * when client sends a global message
					 */
					// Khi client gửi tin nhắn global thì server sẽ gửi nó đến tất cả client khác
					server.getClients().forEach((k,v) -> {
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
					ObjectOutputStream out_ = server.getClients().get(message.getPerson()).getOS();
					out_.writeObject(new Message(message.getMessage(), this.name, MessageType.SERVER_PRIVATE_MESSAGE));
					out_.flush();
				}


			}
		} catch (ClassNotFoundException cnfe) {
			System.out.println("Error establishing connection: " + cnfe.getMessage());
		} catch (SocketException se) {
            System.out.println(name + " at " + socket.getInetAddress().getHostAddress()+" left the Chat!");
            server.getsLabel().setText(server.getsLabel().getText() + "\n\n" + name + " at " + socket.getRemoteSocketAddress()+" left the Chat!");
            server.getClients().remove(name);
			server.getClients().forEach((k,v) -> {
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
	private Socket socket;
	private Server server;
	private GroupChat groupChat;
}
