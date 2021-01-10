package liyunkai;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.awt.*;
import java.awt.event.*;
 
import javax.swing.*;
import javax.swing.border.TitledBorder;
 
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
 

/**
 * @author Eric Li
 * 
 * client
 * Start by registering the class
 * JFrame implements a windowed interface
 * Implement chat and receive server messages
 * JSON packages are introduced to enable the transfer of data between classes
 */
public class Client extends JFrame {
 
	//Accept message box
	private JTextField sendMessage = new JTextField();
	//Display information box
	private JTextArea showMessage = new JTextArea();
	//Display user list
	private JTextArea userlist = new JTextArea(10, 10);
	//IO
	private DataOutputStream message_to_Server;
	private DataInputStream message_from_Server;
	//cs-username
	private String username = null;
	//users list
	private ArrayList<String> list = new ArrayList<>();
	private boolean isKick = false;
	
	private ArrayList<String> commen = new ArrayList<>();

	/**
	 * Client constructor, drawing interface
	 */
	public Client(String username) {
 
		this.username = username;
		
		sendMessage.setFont(new Font("", 0, 18));
		showMessage.setFont(new Font("", Font.BOLD, 18));
		userlist.setFont(new Font("", 0, 18));
 
		//Set input box
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		JLabel jLabel = new JLabel("Add (username-) before message to private chat. Press ENTER to send", JLabel.CENTER);
		jLabel.setFont(new Font("", 0, 11));
		p.add(jLabel, BorderLayout.NORTH);
		p.add(sendMessage, BorderLayout.CENTER);
		sendMessage.setHorizontalAlignment(JTextField.LEFT);
		this.add(p, BorderLayout.SOUTH);
 
		//Add a chat message display box in the middle of the page
		showMessage.add(new JScrollPane());	//scroll bar
		this.add(new JScrollPane(showMessage), BorderLayout.CENTER);
		showMessage.setEditable(false); //not editable
 
		//user list
		final JPanel p2 = new JPanel();
		p2.setLayout(new BorderLayout());
		p2.setBorder(new TitledBorder("Online User"));
		p2.add(new JScrollPane(userlist), BorderLayout.CENTER);
		userlist.setEditable(false); //not editable
		
		this.add(p2, BorderLayout.WEST);
		this.setTitle("Client: " + username);
		this.setSize(800, 500);
		
		int windowWidth = this.getWidth(); //Get window width
        int windowHeight = this.getHeight();//Get window height
        Toolkit kit = Toolkit.getDefaultToolkit(); //Definition toolkit
        Dimension screenSize = kit.getScreenSize(); //Gets the screen size
        int screenWidth = screenSize.width; //Gets the width of the screen
        int screenHeight = screenSize.height; //Gets the height of the screen
        //Fix and center the interface
        this.setLocation(screenWidth/2-windowWidth/2, screenHeight/2-windowHeight/2);
 
		//Listen for input box events
		sendMessage.addActionListener(new ButtonListener());
		
		setVisible(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
		//Package the message into a JSON data format
		JSONObject data = new JSONObject();
		data.put("username", username);
		data.put("msg", null);
 
		try {
			//Create a socket connection server
			Socket socket = new Socket(InetAddress.getLocalHost(), 8080);
			//Get data from the server
			message_from_Server = new DataInputStream(socket.getInputStream());
			//Send data to the server
			message_to_Server = new DataOutputStream(socket.getOutputStream());
			//Send the user name to the server
			message_to_Server.writeUTF(data.toString());
			//Start a thread to read data sent from the server
			ReadThread readThread = new ReadThread();
			readThread.start();
		} catch (IOException ex) {
			//An exception occurred and the connection to the server failed
			showMessage.append("No response from server");
		}
	}
 
	//Listen for the input box message event class
	private class ButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			try {
				//Determine if you are kicked out
				if (isKick) {
					showMessage.append("You've been kicked out\n");
				} else {
					//Set the date format
					SimpleDateFormat data = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					String time = data.format(new Date()).toString();
 
					//Gets input box information
					String msg = sendMessage.getText().trim();
 
					//Do message processing
					if (msg.equals("")) {
						//Prompt sending message cannot be null given
						JOptionPane.showMessageDialog(null, "The message can't be null", "Warning!!!", JOptionPane.ERROR_MESSAGE);
					}else if (msg.equals("STOP")) {
						//The message cannot be sent for STOP to prevent triggering the server to exit with an exception
						JOptionPane.showMessageDialog(null, "The message can't be STOP", "Warning!!!", JOptionPane.ERROR_MESSAGE);
					}else if(msg.equals("EXIT")) {
						//Package the data into JSON format
						JSONObject data_send = new JSONObject();
						data_send.put("username", username);
						data_send.put("msg", msg);
						data_send.put("time", time);
						//Useless account name, set isPrivChat to null
						data_send.put("isPrivChat", "");
						//Send data to server
						message_to_Server.writeUTF(data_send.toString());
						System.exit(EXIT_ON_CLOSE);
					}else {
						//Split the message into the chat content and the user name
						String[] msg1 = msg.split("-");
						//Distinguish between group chats and private chats
						if(msg1.length == 2) {
							if(msg1[1].equals("STATS")) {
								commen.add("STATS ");
								//判断是否为查看指令
								JSONObject state_send = new JSONObject();
								state_send.put("username_from", username);
								state_send.put("msg", "ask_STATS");
								state_send.put("time", time);
								//set check user name
								state_send.put("username_to", msg1[0]);
								//Send data to server
								message_to_Server.writeUTF(state_send.toString());
							}else {
								commen.add("Priv_MESSAGE ");
								//私聊模式
								//Package the data into JSON format
								JSONObject data_send = new JSONObject();
								data_send.put("username", username);
								data_send.put("msg", msg1[1]);
								data_send.put("time", time);
								//Private chat, set isPrivChat to the user_get
								data_send.put("isPrivChat", msg1[0]);
								//Send data to server
								message_to_Server.writeUTF(data_send.toString());
							}
						}else {
							commen.add("BROADCAST ");
							//群发模式
							JSONObject data_send = new JSONObject();
							data_send.put("username", username);
							data_send.put("msg", msg1[0]);
							data_send.put("time", time);
							//Useless account name, set isPrivChat to null
							data_send.put("isPrivChat", "");
							message_to_Server.writeUTF(data_send.toString());
						}
					}
				}
				sendMessage.setText("");
			} catch (Exception ex) {
				System.err.println(ex);
			}
		}
	}
	
	/** 
	 * @author Eric Li
	 *
	 * Read the thread class from the server
	 */
	public class ReadThread extends Thread {
 
		public void run() {
			String json = null;
			try {
				//The wireless loop listens for data sent from the server
				while (true) {
					//Read the data from the server
					json = message_from_Server.readUTF();
					JSONObject data = JSONObject.fromObject(json.toString());
 
					if (json != null) {
						//得到msg并进行判断内容
						String mString = data.getString("msg");

						//Whether been kicked out of a group chat
						if (mString.contains("been kicked out") && mString.contains(username)) {
							commen.add("KICK ");
							isKick = true;
							showMessage.append(username + ",You've been kicked out\n" + "Client will be close in 5s");
							Thread.sleep(5000); //Wait for 5 seconds
							System.exit(EXIT_ON_CLOSE);
						}else if(mString.equals("ask_STATS")) {
							//转发指令列表
							/**
							 * @param username: 发出指令列表的用户
							 * @param isPrivChat: 接受指令的用户
							 * @param msg: 指令列表
							 */					
							JSONObject stats_send = new JSONObject();
							stats_send.put("username", username);
							stats_send.put("isPrivChat", data.get("username_from"));
							stats_send.put("time", data.get("time"));
							stats_send.put("msg", commen);
							//Send data to server
							message_to_Server.writeUTF(stats_send.toString());
						}else {
							//Print chat messages or system prompts
							showMessage.append(mString + "\n\n");
 
							//Force the cursor to move to the bottom
							showMessage.selectAll();

							//Refresh the user list
							list.clear();
							JSONArray jsonArray = data.getJSONArray("userlist");
 
							//Getting a list of users
							for (int i = 0; i < jsonArray.size(); i++) {
								list.add(jsonArray.get(i).toString());
							}
 
							//Print user list
							userlist.setText("User count: " + jsonArray.size() + " \n");
							for (int i = 0; i < list.size(); i++) {
								userlist.append(list.get(i) + "\n");
							}
						}
					}
 
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
 
}
