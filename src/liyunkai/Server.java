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
 * 服务器类，继承JFrame，实现窗口化界面
 */
public class Server extends JFrame {

	// 在线用户列表
	ArrayList<User> clientList = new ArrayList<User>();
	// 在线用户名列表
	ArrayList<String> usernamelist = new ArrayList<String>();
	// 创建一个信息显示框
	private JTextArea jta = new JTextArea();
	// 用于要踢除用户名的输入框
	private JTextField kick_username_input = new JTextField();
	// 踢除用户名
	private String username_kick = null;
	// 声明一个用户对象，该类里面有两个变量 socket，username；
	private User user = null;
	// 声明一个输出流
	DataOutputStream output = null;
	// 声明一个输入流
	DataInputStream input = null;

	public static void main(String[] args) {
		new Server();
	}

	/**
	 * 服务器构造方法,绘画图形界面,监听socket连接
	 */
	public Server() {
		// 设置信息显示框版面
		setLayout(new BorderLayout());
		add(new JScrollPane(jta), BorderLayout.CENTER);
		jta.setEditable(false);
		jta.setFont(new Font("", 0, 18));

		// 设置要踢除用户的输入框
		kick_username_input.setFont(new Font("", 0, 18));

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
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true); //

		try {
			// 创建一个服务器socket，绑定端口8000
			ServerSocket serverSocket = new ServerSocket(8080);
			
			// 打印启动时间
			jta.append("Server startup time: " + new Date() + "\n\n");

			// 无限循环监听是否有新的客户端连接
			while (true) {

				// 监听一个新的连接
				Socket socket = serverSocket.accept();

				// 当有client连接时
				if (socket != null) {

					// 获取用户信息
					input = new DataInputStream(socket.getInputStream());
					String json = input.readUTF();
					JSONObject data = JSONObject.fromObject(json.toString());
					jta.append("Client:" + data.getString("username") + " at:" + new Date() + "login");

					// 显示用户ip
					InetAddress inetAddress = socket.getInetAddress();
					jta.append(", IP address is：" + inetAddress.getHostAddress() + "\n\n");

					// 新建一个用户对象,设置socket，用户名
					user = new User();
					user.setSocket(socket);
					user.setUserName(data.getString("username"));

					// 加入在线用户组列表
					clientList.add(user);

					// 加入用户名列表（用户显示在客户端的用户列表）
					usernamelist.add(data.getString("username"));
				}

				// 用户上线提示，打包成json格式数据
				JSONObject online = new JSONObject();
				online.put("userlist", usernamelist);
				online.put("msg", user.getUserName() + " logged in");

				// 提示所有用户有新的用户上线
				for (int i = 0; i < clientList.size(); i++) {
					try {
						User user = clientList.get(i);
						// 获取每一个用户的socket，得到输出流，
						output = new DataOutputStream(user.getSocket().getOutputStream());
						// 向每个用户端发送数据
						output.writeUTF(online.toString());
					} catch (IOException ex) {
						System.err.println(ex);
					}
				}
				
				// 该socket作为参数，为当前连接用户创建一个线程，用于监听该socket的数据
				HandleAClient task = new HandleAClient(socket);
				new Thread(task).start();
			}
		} catch (IOException ex) {
			System.err.println(ex);
		}
	}

	
	/**
	 * 自定义用户线程类, 判断是否私聊, 提示用户下线
	 */
	class HandleAClient implements Runnable {
		// 已连接的cocket
		private Socket socket;

		public HandleAClient(Socket socket) {
			this.socket = socket;
		}

		public void run() {

			try {
				// 获取本线程监听的socket客户端的输入流
				DataInputStream inputFromClient = new DataInputStream(socket.getInputStream());

				// 循环监听
				while (true) {

					// 获取客户端的数据
					String json = inputFromClient.readUTF();
					JSONObject data = JSONObject.fromObject(json.toString());

					//检测客户端消息是否为EXIT
					//若为EXIT则执行offLine函数
					if(data.getString("msg").equals("EXIT")) {
						for(int j = 0; j < clientList.size(); j++) {
							if (clientList.get(j).getUserName().equals(data.getString("username"))) {
								offLine(j);
							}
						}
					}
					
					// 私聊标记变量
					boolean isPrivate = false;

					// 判断是否私聊
					for (int i = 0; i < clientList.size(); i++) {
						// 私聊，将获取的数据转发给指定用户
						// 通过用户名比较找私聊用户
						if (clientList.get(i).getUserName().equals(data.getString("isPrivChat"))) {

							// 处理聊天内容，这是私聊内容
							String msg = data.getString("username") + " send private to you," + data.getString("time") + ":\n"+ data.getString("msg");

							// 将消息打包成json格式数据发给指定客户端
							packMsg(data, i, msg);
							i++;

							// 标记私聊，结束此次消息发送过程
							isPrivate = true;
							break;
						}
					}

					//群聊
					//将获取的数据转发给每一个用户
					if (isPrivate == false) {
						for (int i = 0; i < clientList.size();) {
							// 将聊天的信息和用户列表打包成json格式数据发给每个客户端
							String msg = data.getString("username") + " " + data.getString("time") + ":\n" + data.getString("msg");
							packMsg(data, i, msg);
							i++;
						}
					}

				}
			} catch (IOException e) {
				System.err.println(e);
			}
		}

		// 将聊天的信息和用户列表打包成json格式数据发给一个客户端
		public void packMsg(JSONObject data, int i, String msg) {
			// 打包数据
			JSONObject chatMessage = new JSONObject();
			chatMessage.put("userlist", usernamelist);
			chatMessage.put("msg", msg);

			// 获取一个用户
			User user = clientList.get(i);

			//向获取用户发送消息
			try {
				output = new DataOutputStream(user.getSocket().getOutputStream());
				output.writeUTF(chatMessage.toString());
			} catch (IOException e) {
				
			}

		}

		// 提示用户下线
		public void offLine(int i) {
			User outuser = clientList.get(i);

			// 从列表中移除
			clientList.remove(i);
			usernamelist.remove(outuser.getUserName());

			// 打包下线的发送消息
			JSONObject out = new JSONObject();
			out.put("userlist", usernamelist);
			out.put("msg", outuser.getUserName() + " exit\n");

			// 提示每个用户有用户下线了
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
	 * 监听输入框
	 * 	服务器下线 STOP
	 * 	踢除指定用户
	 */
	private class ButtonListener implements ActionListener {
		
		public void actionPerformed(ActionEvent e) {
			try {
				// 获取用户名
				username_kick = kick_username_input.getText().trim();

				boolean isUsernameOut = false;
				if (username_kick != null) {
					//服务器下线
					if(username_kick.equals("STOP")) {
							// 打包被踢出的发送消息
							JSONObject out = new JSONObject();
							out.put("userlist", usernamelist);
							out.put("msg", "The server has been stop\n");
	
							//循环userlist来通知每一个客户端服务器将要退出
							for (int j = 0; j < clientList.size(); j++) {
								try {
									User user = clientList.get(j);
									output = new DataOutputStream(user.getSocket().getOutputStream());
									output.writeUTF(out.toString());
								} catch (IOException ex1) {
								}
							}
							//通过系统强制关闭server程序
							System.exit(EXIT_ON_CLOSE);
							return;
					}else {
						//踢出用户
						for (int i = 0; i < clientList.size(); i++) {
							if (clientList.get(i).getUserName().equals(username_kick)) {
								
								//得到提出用户的信息
								User kick_user = clientList.get(i);
	
								// 打包被踢出的发送消息
								JSONObject out = new JSONObject();
								out.put("userlist", usernamelist);
								out.put("msg", kick_user.getUserName() + " been kicked out\n");
	
								// 提示每个用户有用户被踢出了
								for (int j = 0; j < clientList.size(); j++) {
									try {
										User user = clientList.get(j);
										output = new DataOutputStream(user.getSocket().getOutputStream());
										output.writeUTF(out.toString());
									} catch (IOException ex1) {
									}
								}
	
								// 从列表中移除
								clientList.remove(i);
								usernamelist.remove(kick_user.getUserName());
	
								isUsernameOut = true;
								break;
							}
	
						}
						//未找到用户
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