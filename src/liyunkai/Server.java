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
 * �������࣬�̳�JFrame��ʵ�ִ��ڻ�����
 */
public class Server extends JFrame {

	// �����û��б�
	ArrayList<User> clientList = new ArrayList<User>();
	// �����û����б�
	ArrayList<String> usernamelist = new ArrayList<String>();
	// ����һ����Ϣ��ʾ��
	private JTextArea jta = new JTextArea();
	// ����Ҫ�߳��û����������
	private JTextField kick_username_input = new JTextField();
	// �߳��û���
	private String username_kick = null;
	// ����һ���û����󣬸����������������� socket��username��
	private User user = null;
	// ����һ�������
	DataOutputStream output = null;
	// ����һ��������
	DataInputStream input = null;

	public static void main(String[] args) {
		new Server();
	}

	/**
	 * ���������췽��,�滭ͼ�ν���,����socket����
	 */
	public Server() {
		// ������Ϣ��ʾ�����
		setLayout(new BorderLayout());
		add(new JScrollPane(jta), BorderLayout.CENTER);
		jta.setEditable(false);
		jta.setFont(new Font("", 0, 18));

		// ����Ҫ�߳��û��������
		kick_username_input.setFont(new Font("", 0, 18));

		final JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		JLabel jLabel = new JLabel("Enter the user you want to kick��press ENTER.");
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
			// ����һ��������socket���󶨶˿�8000
			ServerSocket serverSocket = new ServerSocket(8080);
			
			// ��ӡ����ʱ��
			jta.append("Server startup time: " + new Date() + "\n\n");

			// ����ѭ�������Ƿ����µĿͻ�������
			while (true) {

				// ����һ���µ�����
				Socket socket = serverSocket.accept();

				// ����client����ʱ
				if (socket != null) {

					// ��ȡ�û���Ϣ
					input = new DataInputStream(socket.getInputStream());
					String json = input.readUTF();
					JSONObject data = JSONObject.fromObject(json.toString());
					jta.append("Client:" + data.getString("username") + " at:" + new Date() + "login");

					// ��ʾ�û�ip
					InetAddress inetAddress = socket.getInetAddress();
					jta.append(", IP address is��" + inetAddress.getHostAddress() + "\n\n");

					// �½�һ���û�����,����socket���û���
					user = new User();
					user.setSocket(socket);
					user.setUserName(data.getString("username"));

					// ���������û����б�
					clientList.add(user);

					// �����û����б��û���ʾ�ڿͻ��˵��û��б�
					usernamelist.add(data.getString("username"));
				}

				// �û�������ʾ�������json��ʽ����
				JSONObject online = new JSONObject();
				online.put("userlist", usernamelist);
				online.put("msg", user.getUserName() + " logged in");

				// ��ʾ�����û����µ��û�����
				for (int i = 0; i < clientList.size(); i++) {
					try {
						User user = clientList.get(i);
						// ��ȡÿһ���û���socket���õ��������
						output = new DataOutputStream(user.getSocket().getOutputStream());
						// ��ÿ���û��˷�������
						output.writeUTF(online.toString());
					} catch (IOException ex) {
						System.err.println(ex);
					}
				}
				
				// ��socket��Ϊ������Ϊ��ǰ�����û�����һ���̣߳����ڼ�����socket������
				HandleAClient task = new HandleAClient(socket);
				new Thread(task).start();
			}
		} catch (IOException ex) {
			System.err.println(ex);
		}
	}

	
	/**
	 * �Զ����û��߳���, �ж��Ƿ�˽��, ��ʾ�û�����
	 */
	class HandleAClient implements Runnable {
		// �����ӵ�cocket
		private Socket socket;

		public HandleAClient(Socket socket) {
			this.socket = socket;
		}

		public void run() {

			try {
				// ��ȡ���̼߳�����socket�ͻ��˵�������
				DataInputStream inputFromClient = new DataInputStream(socket.getInputStream());

				// ѭ������
				while (true) {

					// ��ȡ�ͻ��˵�����
					String json = inputFromClient.readUTF();
					JSONObject data = JSONObject.fromObject(json.toString());

					//���ͻ�����Ϣ�Ƿ�ΪEXIT
					//��ΪEXIT��ִ��offLine����
					if(data.getString("msg").equals("EXIT")) {
						for(int j = 0; j < clientList.size(); j++) {
							if (clientList.get(j).getUserName().equals(data.getString("username"))) {
								offLine(j);
							}
						}
					}
					
					// ˽�ı�Ǳ���
					boolean isPrivate = false;

					// �ж��Ƿ�˽��
					for (int i = 0; i < clientList.size(); i++) {
						// ˽�ģ�����ȡ������ת����ָ���û�
						// ͨ���û����Ƚ���˽���û�
						if (clientList.get(i).getUserName().equals(data.getString("isPrivChat"))) {

							// �����������ݣ�����˽������
							String msg = data.getString("username") + " send private to you," + data.getString("time") + ":\n"+ data.getString("msg");

							// ����Ϣ�����json��ʽ���ݷ���ָ���ͻ���
							packMsg(data, i, msg);
							i++;

							// ���˽�ģ������˴���Ϣ���͹���
							isPrivate = true;
							break;
						}
					}

					//Ⱥ��
					//����ȡ������ת����ÿһ���û�
					if (isPrivate == false) {
						for (int i = 0; i < clientList.size();) {
							// ���������Ϣ���û��б�����json��ʽ���ݷ���ÿ���ͻ���
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

		// ���������Ϣ���û��б�����json��ʽ���ݷ���һ���ͻ���
		public void packMsg(JSONObject data, int i, String msg) {
			// �������
			JSONObject chatMessage = new JSONObject();
			chatMessage.put("userlist", usernamelist);
			chatMessage.put("msg", msg);

			// ��ȡһ���û�
			User user = clientList.get(i);

			//���ȡ�û�������Ϣ
			try {
				output = new DataOutputStream(user.getSocket().getOutputStream());
				output.writeUTF(chatMessage.toString());
			} catch (IOException e) {
				
			}

		}

		// ��ʾ�û�����
		public void offLine(int i) {
			User outuser = clientList.get(i);

			// ���б����Ƴ�
			clientList.remove(i);
			usernamelist.remove(outuser.getUserName());

			// ������ߵķ�����Ϣ
			JSONObject out = new JSONObject();
			out.put("userlist", usernamelist);
			out.put("msg", outuser.getUserName() + " exit\n");

			// ��ʾÿ���û����û�������
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
	 * ���������
	 * 	���������� STOP
	 * 	�߳�ָ���û�
	 */
	private class ButtonListener implements ActionListener {
		
		public void actionPerformed(ActionEvent e) {
			try {
				// ��ȡ�û���
				username_kick = kick_username_input.getText().trim();

				boolean isUsernameOut = false;
				if (username_kick != null) {
					//����������
					if(username_kick.equals("STOP")) {
							// ������߳��ķ�����Ϣ
							JSONObject out = new JSONObject();
							out.put("userlist", usernamelist);
							out.put("msg", "The server has been stop\n");
	
							//ѭ��userlist��֪ͨÿһ���ͻ��˷�������Ҫ�˳�
							for (int j = 0; j < clientList.size(); j++) {
								try {
									User user = clientList.get(j);
									output = new DataOutputStream(user.getSocket().getOutputStream());
									output.writeUTF(out.toString());
								} catch (IOException ex1) {
								}
							}
							//ͨ��ϵͳǿ�ƹر�server����
							System.exit(EXIT_ON_CLOSE);
							return;
					}else {
						//�߳��û�
						for (int i = 0; i < clientList.size(); i++) {
							if (clientList.get(i).getUserName().equals(username_kick)) {
								
								//�õ�����û�����Ϣ
								User kick_user = clientList.get(i);
	
								// ������߳��ķ�����Ϣ
								JSONObject out = new JSONObject();
								out.put("userlist", usernamelist);
								out.put("msg", kick_user.getUserName() + " been kicked out\n");
	
								// ��ʾÿ���û����û����߳���
								for (int j = 0; j < clientList.size(); j++) {
									try {
										User user = clientList.get(j);
										output = new DataOutputStream(user.getSocket().getOutputStream());
										output.writeUTF(out.toString());
									} catch (IOException ex1) {
									}
								}
	
								// ���б����Ƴ�
								clientList.remove(i);
								usernamelist.remove(kick_user.getUserName());
	
								isUsernameOut = true;
								break;
							}
	
						}
						//δ�ҵ��û�
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