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
 * 客户端
 * 通过注册类启动
 * JFrame实现窗口化界面
 * 实现聊天和接受服务器消息
 * 
 * 引入JSON包来实现数据在不同类之间的传输
 */
public class Client extends JFrame {
 
	// 接受消息框
	private JTextField sendMessage = new JTextField();
	// 显示信息框
	private JTextArea showMessage = new JTextArea();
	// 显示用户列表
	private JTextArea userlist = new JTextArea(10, 10);
	// IO
	private DataOutputStream message_to_Server;
	private DataInputStream message_from_Server;
	// 客户端用户名
	private String username = null;
	// 用户列表
	private ArrayList<String> list = new ArrayList<>();
	private boolean isKick = false;

	/**
	 * 客户端构造函数，绘画界面
	 * 
	 * @param username: 传入用户名
	 */
	public Client(String username) {
 
		this.username = username;
		
		sendMessage.setFont(new Font("", 0, 18));
		showMessage.setFont(new Font("", Font.BOLD, 18));
		userlist.setFont(new Font("", 0, 18));
 
		// 设置输入框
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		JLabel jLabel = new JLabel("Add (username-) before message to private chat. Press ENTER to send", JLabel.CENTER);
		jLabel.setFont(new Font("", 0, 11));
		p.add(jLabel, BorderLayout.NORTH);
		p.add(sendMessage, BorderLayout.CENTER);
		sendMessage.setHorizontalAlignment(JTextField.LEFT);
		this.add(p, BorderLayout.SOUTH);
 
		// 在版面的中间增加一个聊天信息显示框
		showMessage.add(new JScrollPane());	//让聊天记录栏带有滚动条
		this.add(new JScrollPane(showMessage), BorderLayout.CENTER);
		showMessage.setEditable(false); //不可编辑
 
		// 用户列表
		final JPanel p2 = new JPanel();
		p2.setLayout(new BorderLayout());
		p2.setBorder(new TitledBorder("Online User"));
		p2.add(new JScrollPane(userlist), BorderLayout.CENTER);
		userlist.setEditable(false); //不可编辑
		
		this.add(p2, BorderLayout.WEST);
		this.setTitle("Client: " + username);
		this.setSize(800, 500);
		
		int windowWidth = this.getWidth(); //获得窗口宽
        int windowHeight = this.getHeight();//获得窗口高
        Toolkit kit = Toolkit.getDefaultToolkit(); //定义工具包
        Dimension screenSize = kit.getScreenSize(); //获取屏幕的尺寸
        int screenWidth = screenSize.width; //获取屏幕的宽
        int screenHeight = screenSize.height; //获取屏幕的高
        //固定将界面居中
        this.setLocation(screenWidth/2-windowWidth/2, screenHeight/2-windowHeight/2);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
		// 监听输入框事件
		sendMessage.addActionListener(new ButtonListener());
		
		setVisible(true);
 
		// 将消息打包成json数据格式
		JSONObject data = new JSONObject();
		data.put("username", username);
		data.put("msg", null);
 
		try {
			//创建一个socket连接服务器
			Socket socket = new Socket(InetAddress.getLocalHost(), 8080);
 
			//获取服务器的数据
			message_from_Server = new DataInputStream(socket.getInputStream());
 
			//向服务器发送数据
			message_to_Server = new DataOutputStream(socket.getOutputStream());
 
			//向服务器发送 用户名
			message_to_Server.writeUTF(data.toString());
 
			//开启一个线程，用于读取服务器发送过来的数据
			ReadThread readThread = new ReadThread();
			readThread.start();
 
		} catch (IOException ex) {
			//出现异常，连接服务器失败
			showMessage.append("No response from server");
		}
	}
 
	// 监听输入框消息事件类
	private class ButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			try {
				//首先判断是否被踢出
				if (isKick) {
					showMessage.append("You've been kicked out\n");
				} else {
					// 设置日期格式
					SimpleDateFormat data = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 设置日期格式
					String time = data.format(new Date()).toString();
 
					// 获取输入框信息
					String msg = sendMessage.getText().trim();
 
					//进行消息处理
					if (msg.equals("")) {
						//提示发送消息不能为空给
						JOptionPane.showMessageDialog(null, "The message can't be null", "Warning!!!", JOptionPane.ERROR_MESSAGE);
					}else if (msg.equals("STOP")) {
						//提示发送消息不能为STOP，防止触发服务器异常退出
						JOptionPane.showMessageDialog(null, "The message can't be STOP", "Warning!!!", JOptionPane.ERROR_MESSAGE);
					}else if(msg.equals("EXIT")) {
						// 打包数据成json格式
						JSONObject data_send = new JSONObject();
						data_send.put("username", username);
						data_send.put("msg", msg);
						data_send.put("time", time);
						// 无用户名，将isPrivChat设置空值
						data_send.put("isPrivChat", "");
						// 向服务器发送数据
						message_to_Server.writeUTF(data_send.toString());
						System.exit(EXIT_ON_CLOSE);
					}else {
						//将消息拆分为聊天内容和用户名
						String[] msg1 = msg.split("-");
						//区分群聊或者私聊
						if(msg1.length == 2) {
							//打包数据成json格式
							JSONObject data_send = new JSONObject();
							data_send.put("username", username);
							data_send.put("msg", msg1[1]);
							data_send.put("time", time);
							// 私聊，将isPrivChat设置成用户名
							data_send.put("isPrivChat", msg1[0]);
							// 向服务器发送数据
							message_to_Server.writeUTF(data_send.toString());
						}else {
							// 打包数据成json格式
							JSONObject data_send = new JSONObject();
							data_send.put("username", username);
							data_send.put("msg", msg1[0]);
							data_send.put("time", time);
							// 无用户名，将isPrivChat设置空值
							data_send.put("isPrivChat", "");
							// 向服务器发送数据
							message_to_Server.writeUTF(data_send.toString());
						}
					}
				}
				//重置输入框为空
				sendMessage.setText("");
			} catch (Exception ex) {
				System.err.println(ex);
			}
		}
	}
	
	// 读取服务器发来消息的线程类
	public class ReadThread extends Thread {
 
		public void run() {
			String json = null;
			try {
				// 无线循环监听服务器发来的数据
				while (true) {
					// 读取服务器的数据
					json = message_from_Server.readUTF();
					// 转化成json格式
					JSONObject data = JSONObject.fromObject(json.toString());
 
					if (json != null) {
						String mString = data.getString("msg");
 
						// 是否被踢出群聊
						if (mString.contains("been kicked out") && mString.contains(username)) {
							isKick = true;
							showMessage.append(username + ",You've been kicked out\n"
									+ "Client will be close in 5s");
							Thread.sleep(5000);//单位：毫秒
							System.exit(EXIT_ON_CLOSE);
						}else {
							// 打印聊天信息或者系统提示信息
							showMessage.append(mString + "\n\n");
 
							// 强制使光标移动最底部
							showMessage.selectAll();

							// 刷新用户列表
							list.clear();
							JSONArray jsonArray = data.getJSONArray("userlist");
 
							// 获取用户列表
							for (int i = 0; i < jsonArray.size(); i++) {
								list.add(jsonArray.get(i).toString());
							}
 
							// 打印用户列表
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
