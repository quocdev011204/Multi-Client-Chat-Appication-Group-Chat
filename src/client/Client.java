/**
 *
 */
package client;

import java.net.*;

import javax.swing.*;

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


public class Client {

	/*
	 * main method of the Main-Class of the Client.jar file
	 */
	public static void main(String[] args) throws IOException {
		// Gọi đến constructor Client
		new Client();
	}

	/*
	 * connect to the server, store input/output streams and setup GUI for client interaction
	 */
	Client() {

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					// Gọi đến startConnection để kết nối với server
					startConnection();
					// Gọi đến startChatGUI để thiết lập giao diẹn trò chuyện
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
		String str = "";
		String message = "Enter Host name of the server machine :";
		while(str.isEmpty()) {
			str = (String)JOptionPane.showInputDialog(frame, message, InetAddress.getLocalHost().getHostName());
			if(str == null) System.exit(0);
			message = "Host name cannot be empty!\nEnter Host name of the server machine:";
		}
		str = str.trim();
		server = str;
		System.out.println("Connecting to server...");
		message = "Using port number 50000\nTo connect to a different port, type the port number:\n";
		String str_ = (String)JOptionPane.showInputDialog(frame, message, "50000");
		if(str_ == null) System.exit(0);
		str_ = str_.trim();
		pNumber = Integer.parseInt(str_);
		// Sử dụng SwingWorker để thực hiện kết nối mạng trong nền, không làm đông GUI
		new SwingWorker<Object, Object>() {

			@Override
			protected Object doInBackground() throws Exception {
				// Tạo một Socket để kết nối đến server với địa chỉ ip và số port đã nhập
				socket = new Socket(server, pNumber);
				System.out.println("Connected");
				return null;
			}

		}.execute();
	}

	/*
	 * generate GUI for user interaction
	 */
	// Bắt đầu trò chuyện
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
		System.out.println(socket == null);
		new SwingWorker<Object, Object>() {

			@Override
			protected Object doInBackground() throws Exception {
				Client.this.out = new ObjectOutputStream(socket.getOutputStream());
				Client.this.in = new ObjectInputStream(socket.getInputStream());
				out.writeObject(new Message(name, MessageType.SEND_NAME));
				out.flush();
				return null;
			}

		}.execute();

		/*
		 * start a helper thread that keeps on reading messages from server and also sets up private chatting GUI
		 */
		new Thread(new ClientHelper(this)).start();

		/*
		 * set global chatting GUI
		 */
		frame.setTitle("Connected to server at: " + socket.getRemoteSocketAddress().toString());
		// JSplitPane dùng để chia thành 2 phần
		splitPane = new JSplitPane();
		splitPane.setBorder(BorderFactory.createTitledBorder(str));
		leftPanel = new JPanel();
		rightPanel = new JPanel();
		// Thanh Scroll
		globalChat = new JScrollPane();
		// Để hiển thị tin nhắn toàn hệ thống
		globalChatArea = new JTextArea();
		globalChatArea.setEditable(false);
		globalChatArea.setFont(globalChatArea.getFont().deriveFont(18f));
		globalChatArea.setBackground(new Color(77, 149, 37));
		messageArea = new JScrollPane();
		messageText = new JTextArea();
		// Button gửi tin nhắn
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


		// Tạo middlePanel
		JPanel middlePanel = new JPanel();
		middlePanel.setLayout(new GridBagLayout());
		middlePanel.setPreferredSize(new Dimension(500, 600));
		middlePanel.setBorder(BorderFactory.createTitledBorder("Middle Panel")); // Đặt tiêu đề cho middlePanel

		// Tạo nút "Join Group" và "Create Group"
		JButton joinGroupButton = new JButton("Join Group");
		JButton createGroupButton = new JButton("Create Group");

		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.fill = GridBagConstraints.HORIZONTAL; // Cho phép nút mở rộng theo chiều ngang
		gbc.weightx = 1.0; // Cho phép nút mở rộng theo chiều ngang
		gbc.weighty = 0.0; // Không cho phép mở rộng theo chiều dọc (nút Join Group)
		gbc.gridx = 0; // Cột 0
		gbc.gridy = 0; // Hàng 0
		gbc.anchor = GridBagConstraints.NORTH; // Đặt nút ở phía trên cùng
		middlePanel.add(joinGroupButton, gbc); // Thêm nút Join Group

// Đặt nút Create Group ngay phía dưới nút Join Group
		gbc.gridy = 1; // Hàng 1 (ngay bên dưới Join Group)
		gbc.weighty = 1.0; // Để khoảng trống giữa nút Create Group và phần dưới
		middlePanel.add(createGroupButton, gbc);


		joinGroupButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Xử lý sự kiện cho nút "Join Group"
				JOptionPane.showMessageDialog(frame, "Join Group button clicked!");
			}
		});

		createGroupButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Xử lý sự kiện cho nút "Create Group"
				JOptionPane.showMessageDialog(frame, "Create Group button clicked!");
			}
		});

		// Tạo một JSplitPane mới cho leftPanel và middlePanel
		JSplitPane leftMiddleSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, middlePanel);
		leftMiddleSplitPane.setDividerLocation(350); // Đặt vị trí chia
		leftMiddleSplitPane.setResizeWeight(0.5); // Tỉ lệ điều chỉnh khi thay đổi kích thước

		// Cập nhật splitPane chính để bao gồm leftMiddleSplitPane và rightPanel
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftMiddleSplitPane, rightPanel);
		splitPane.setDividerLocation(700);



		frame.setPreferredSize(new Dimension(1200,600));
		frame.setLayout(new GridLayout());
		frame.add(splitPane);
		frame.pack();
		frame.setVisible(true);
		System.out.println("visible");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		out.writeObject(new Message());
		out.flush();
	}

	/*
	 * helper method for adding gridbaglayout constraints
	 */
	// Thêm các thành phần vào Container với rằng buộc GridBagContrains đẻ xác định cụ thể vị trí của ác các thành phần
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
	private int pNumber;
	private String server;
}