/**
 * 
 */
package server;

// mạng
import java.awt.*;
import java.net.*;

//I/O
import java.io.*;
import java.util.*;

// Giao diện GUI
import javax.swing.*;

import objects.Group;
import objects.Streams;

// Khai báo lớp server
public class Server {
	
	 /**
	  * main method of the Main-Class of the jar file
	  */
	public static void main(String[] args) {
		new Server();	
	} // gọi đến constructor server
	
	/*
	 * start Listening through any available port, generate GUI to inform the user of server running
	 * and start listening through that port
	 */
	Server() {
		// Khởi tạo Hashtable để lưu trữ client
		clients = new Hashtable<String,Streams>();
		String message = "Using port number 50000\nTo listen to clients through a different port, type the port number:\n";
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {						
				String str = (String)JOptionPane.showInputDialog(frame, message, "50000");
				if(str == null) System.exit(0);			
				str = str.trim();
				pNumber = Integer.parseInt(str);
						try {
							Server.this.server = new ServerSocket(pNumber);
						} catch(IOException ioe) {
							System.out.println("Could't connect to server: " + ioe.getMessage());
						}
						if(server == null) {							
							JOptionPane.showMessageDialog(frame, "Port in Use!", "Error!" , JOptionPane.ERROR_MESSAGE);

							System.exit(0);
						}

							setGUI(); // gọi setGUI để cấu hình giao diện

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
			sLabel.setFont(new Font("Arial", Font.PLAIN, 15));
		} catch (UnknownHostException uhe) {
			uhe.printStackTrace();
		}
		frame.add(sLabelPane);
		frame.setPreferredSize(new Dimension(600,500));
		frame.setResizable(false);
		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	
		startListening();
	}

	public void createGroup(String groupName) {
		groups.put(groupName, new Group(groupName));
	}
	
	/*
	 * start listening to specified port forever
	 */
	// Bắt đầu lắng nghe kết nối từ client
	private void startListening() {
		// SwingWorker dùng để xử lý các tác vụ nền(kết nối client) trong một luồng riêng biệt
		new SwingWorker<Object,Object>() {

			@Override
			protected Object doInBackground() throws Exception {
				// Vòng lặp vô hạn chấp nhận kết nối từ Client
				while(true) {			
					try {
						// server.accept() dùng để chờ và chấp nhận kết nối từ client
						Socket socket = server.accept();
						// Mỗi kết nối được chấp nhận thì tạo ra một luồng ClientHanlder để xử lý trực tiếp với client
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
	// Yêu cầu người dùng nhập số port.
	//Tạo một ServerSocket để lắng nghe kết nối từ client trên port đó.
	//Cấu hình giao diện người dùng để hiển thị thông tin về server.
	//Liên tục chấp nhận kết nối từ client trong một luồng nền và xử lý từng client trong một luồng riêng.
}
