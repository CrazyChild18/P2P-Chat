package liyunkai;

import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import net.sf.json.JSONObject;

/**
 * @author Eric Li
 * 
 * The server class inherits the JFrame and implements the windowed interface
 * Responsible for receiving and distributing messages to the server
 * 
 * {@code}ArrayList<User>: Use list to store users
 * {@code}DataOutputStream: output stream
 * {@code}DataInputStream: input stream
 */
public class Server extends JFrame {

	//List of online users
	ArrayList<User> clientList = new ArrayList<User>();
	//List of online user names
	ArrayList<String> usernamelist = new ArrayList<String>();
	//Information display box
	private JTextArea jta = new JTextArea();
	//Used to kick out the input field of the user name
	private JTextField kick_username_input = new JTextField();
	//Kick out the user name
	private String username_kick = null;
	//User object, which has two variables socket and username
	private User user = null;
	//Declare an output stream
	DataOutputStream output = null;
	//Declare an input stream
	DataInputStream input = null;

	public static void main(String[] args) {
		new Server();
	}

	/**
	 * Server construction method
	 * drawing graphical interface
	 * listening socket connection
	 */
	public Server() {
		//Set the information display box layout
		setLayout(new BorderLayout());
		add(new JScrollPane(jta), BorderLayout.CENTER);
		jta.setEditable(false);
		jta.setFont(new Font("", 0, 18));

		//Set input box
		kick_username_input.setFont(new Font("", 0, 17));

		final JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		JLabel jLabel = new JLabel("Enter the user you want to kick，press ENTER.");
		jLabel.setFont(new Font("", 0, 15));
		p.add(jLabel, BorderLayout.WEST);
		p.add(kick_username_input, BorderLayout.CENTER);
		kick_username_input.setHorizontalAlignment(JTextField.LEFT);
		add(p, BorderLayout.SOUTH);

		kick_username_input.addActionListener(new ButtonListener());

		setTitle("Server");
		setSize(700, 400);
		setLocation(200, 300);
		setVisible(true); //

		try {
			//Create a server socket, bind port 8000
			ServerSocket serverSocket = new ServerSocket(8080);
			
			//Print startup time
			jta.append("Server startup time: " + new Date() + "\n\n");

			//Infinite loop listens for new client connections
			while (true) {

				//Listen for a new connection
				Socket socket = serverSocket.accept();

				//When there is client connected
				if (socket != null) {

					//get user info
					input = new DataInputStream(socket.getInputStream());
					String json = input.readUTF();
					JSONObject data = JSONObject.fromObject(json.toString());
					jta.append("Client: " + data.getString("username") + " at:" + new Date() + " login");

					//Display user IP
					InetAddress inetAddress = socket.getInetAddress();
					jta.append(", IP address is：" + inetAddress.getHostAddress() + "\n\n");

					//Create a new user object, set socket, user name
					user = new User();
					user.setSocket(socket);
					user.setUserName(data.getString("username"));

					//Join the list of online user groups
					clientList.add(user);

					//Add user name list (users are displayed in the client user list)
					usernamelist.add(data.getString("username"));
				}

				//When users are prompted to go online, package the data into JSON format
				JSONObject online = new JSONObject();
				online.put("userlist", usernamelist);
				online.put("msg", user.getUserName() + " logged in");

				//Prompt all users to have new users online
				for (int i = 0; i < clientList.size(); i++) {
					try {
						User user = clientList.get(i);
						//Get each user socket, get the output stream
						output = new DataOutputStream(user.getSocket().getOutputStream());
						//Send data to each client
						output.writeUTF(online.toString());
					} catch (IOException ex) {
						System.err.println(ex);
					}
				}
				
				//Socket as a parameter
				//Create a thread for the current connected user to listen for the socket's data
				HandleAClient task = new HandleAClient(socket);
				new Thread(task).start();
			}
		} catch (IOException ex) {
			System.err.println(ex);
		}
	}

	
	/**
	 * Custom user thread class
	 * determine whether private chat
	 * prompt the user to log off
	 */
	class HandleAClient implements Runnable {
		//Connected sockets
		private Socket socket;

		public HandleAClient(Socket socket) {
			this.socket = socket;
		}

		public void run() {

			try {
				//Gets the input stream from the socket client that this thread is listening to
				DataInputStream inputFromClient = new DataInputStream(socket.getInputStream());

				//Cycle to monitor
				while (true) {

					//Get the client data
					String json = inputFromClient.readUTF();
					JSONObject data = JSONObject.fromObject(json.toString());
										
					if(data.getString("msg").equals("EXIT")) {
						//If is EXIT, the offLine function is executed
						for(int i = 0; i < clientList.size(); i++) {
							if (clientList.get(i).getUserName().equals(data.getString("username"))) {
								offLine(i);
							}
						}
					}else if(data.getString("msg").equals("ask_STATS")) {
						//STATS
						for(int i=0; i<clientList.size(); i++) {
							if(clientList.get(i).getUserName().equals(data.getString("username_to"))) {	
								//转发
								JSONObject stats_msg = new JSONObject();
								stats_msg.put("username_to", data.get("username_to"));
								stats_msg.put("msg", "ask_STATS");
								stats_msg.put("time", data.get("time"));
								stats_msg.put("username_from", data.get("username_from"));
								//找到被查看的客户端,发送查询指令
								try {
									User user = clientList.get(i);
									output = new DataOutputStream(user.getSocket().getOutputStream());
									output.writeUTF(stats_msg.toString());
								} catch (IOException ex1) {
									System.out.println("STATS error: " + ex1.getMessage());
								}
							}
						}
					}else {
						// marks for private chat
						boolean isPrivate = false;

						// Private chat, the acquired data forward to the designated user
						for (int i = 0; i < clientList.size(); i++) {
							//Find a private chat user by comparing user names
							if (clientList.get(i).getUserName().equals(data.getString("isPrivChat"))) {

								//Handling chat content
								String msg = data.getString("username") + " send to you," + 
										data.getString("time") + ":\n"+ data.getString("msg");

								//Packages the message into JSON format and sends the data to the specified client
								packMsg(data, i, msg);
								i++;

								//Mark the private chat to end the message sending process
								isPrivate = true;
								break;
							}
						}

						//group chat
						//Forward the acquired data to each user
						if (isPrivate == false) {
							for (int i = 0; i < clientList.size();) {
								//The chat information and user list are packaged into JSON format and sent to each client
								String msg = data.getString("username") + " " + data.getString("time") + ":\n" 
										+ data.getString("msg");
								packMsg(data, i, msg);
								i++;
							}
						}
					}
				}
			} catch (IOException e) {
				System.err.println(e);
			}
		}

		//The chat information and user list are packaged into JSON format and sent to a client
		public void packMsg(JSONObject data, int i, String msg) {
			//packing data 
			JSONObject chatMessage = new JSONObject();
			chatMessage.put("userlist", usernamelist);
			chatMessage.put("msg", msg);

			//Get a user
			User user = clientList.get(i);

			//Send a message to the fetch user
			try {
				output = new DataOutputStream(user.getSocket().getOutputStream());
				output.writeUTF(chatMessage.toString());
			} catch (IOException e) {
				//Prevents the client from shutting down directly rather than quitting using instructions
				offLine(i);
			}

		}

		//Prompt log off
		public void offLine(int i) {
			User outuser = clientList.get(i);

			//Removed from the list
			clientList.remove(i);
			usernamelist.remove(outuser.getUserName());

			//Package the outgoing message that goes offline
			JSONObject out = new JSONObject();
			out.put("userlist", usernamelist);
			out.put("msg", outuser.getUserName() + " exit\n");

			//Prompt each user to log off
			for (int j = 0; j < clientList.size(); j++) {
				try {
					User user = clientList.get(j);
					output = new DataOutputStream(user.getSocket().getOutputStream());
					output.writeUTF(out.toString());
				} catch (IOException ex1) {
				}
			}
		}
	}
	

	/**
	 * Monitor input box
	 * 	The server goes offline (STOP)
	 * 	Kicks out the specified user
	 */
	private class ButtonListener implements ActionListener {
		
		public void actionPerformed(ActionEvent e) {
			try {
				//Get UserName
				username_kick = kick_username_input.getText().trim();

				boolean isUsernameOut = false;
				if (username_kick != null) {
					//Server offline
					if(username_kick.equals("STOP")) {
							// 打包被踢出的发送消息
							JSONObject out = new JSONObject();
							out.put("userlist", usernamelist);
							out.put("msg", "The server has been stop\n");
	
							//Loop the UserList to notify each client server that it is quitting
							for (int j = 0; j < clientList.size(); j++) {
								try {
									User user = clientList.get(j);
									output = new DataOutputStream(user.getSocket().getOutputStream());
									output.writeUTF(out.toString());
								} catch (IOException ex1) {
								}
							}
							//Force the server program to shut down through the system
							System.exit(EXIT_ON_CLOSE);
							return;
					}else {
						//kick User
						for (int i = 0; i < clientList.size(); i++) {
							if (clientList.get(i).getUserName().equals(username_kick)) {
								
								//Get the user's information
								User kick_user = clientList.get(i);
	
								usernamelist.remove(kick_user.getUserName());
								
								//Package the send message that is kicked out
								JSONObject out = new JSONObject();
								out.put("userlist", usernamelist);
								out.put("msg", kick_user.getUserName() + " been kicked out\n");
	
								//Prompt each user that a user has been kicked out
								for (int j = 0; j < clientList.size(); j++) {
									try {
										User user = clientList.get(j);
										output = new DataOutputStream(user.getSocket().getOutputStream());
										output.writeUTF(out.toString());
									} catch (IOException ex1) {
									}
								}
	
								//Removed from the list
								clientList.remove(i);
	
								isUsernameOut = true;
								break;
							}
	
						}
						//User not found
						if (isUsernameOut == false) {
							jta.append("Not found user: " + username_kick + "\n");
						}
					}
	
					kick_username_input.setText("");
				}
			} catch (Exception ex) {
				System.err.println(ex);
			}
		}
	}
}