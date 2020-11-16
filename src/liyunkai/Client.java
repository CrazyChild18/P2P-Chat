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
 * �ͻ���
 * ͨ��ע��������
 * JFrameʵ�ִ��ڻ�����
 * ʵ������ͽ��ܷ�������Ϣ
 * 
 * ����JSON����ʵ�������ڲ�ͬ��֮��Ĵ���
 */
public class Client extends JFrame {
 
	// ������Ϣ��
	private JTextField sendMessage = new JTextField();
	// ��ʾ��Ϣ��
	private JTextArea showMessage = new JTextArea();
	// ��ʾ�û��б�
	private JTextArea userlist = new JTextArea(10, 10);
	// IO
	private DataOutputStream message_to_Server;
	private DataInputStream message_from_Server;
	// �ͻ����û���
	private String username = null;
	// �û��б�
	private ArrayList<String> list = new ArrayList<>();
	private boolean isKick = false;

	/**
	 * �ͻ��˹��캯�����滭����
	 * 
	 * @param username: �����û���
	 */
	public Client(String username) {
 
		this.username = username;
		
		sendMessage.setFont(new Font("", 0, 18));
		showMessage.setFont(new Font("", Font.BOLD, 18));
		userlist.setFont(new Font("", 0, 18));
 
		// ���������
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		JLabel jLabel = new JLabel("Add (username-) before message to private chat. Press ENTER to send", JLabel.CENTER);
		jLabel.setFont(new Font("", 0, 11));
		p.add(jLabel, BorderLayout.NORTH);
		p.add(sendMessage, BorderLayout.CENTER);
		sendMessage.setHorizontalAlignment(JTextField.LEFT);
		this.add(p, BorderLayout.SOUTH);
 
		// �ڰ�����м�����һ��������Ϣ��ʾ��
		showMessage.add(new JScrollPane());	//�������¼�����й�����
		this.add(new JScrollPane(showMessage), BorderLayout.CENTER);
		showMessage.setEditable(false); //���ɱ༭
 
		// �û��б�
		final JPanel p2 = new JPanel();
		p2.setLayout(new BorderLayout());
		p2.setBorder(new TitledBorder("Online User"));
		p2.add(new JScrollPane(userlist), BorderLayout.CENTER);
		userlist.setEditable(false); //���ɱ༭
		
		this.add(p2, BorderLayout.WEST);
		this.setTitle("Client: " + username);
		this.setSize(800, 500);
		
		int windowWidth = this.getWidth(); //��ô��ڿ�
        int windowHeight = this.getHeight();//��ô��ڸ�
        Toolkit kit = Toolkit.getDefaultToolkit(); //���幤�߰�
        Dimension screenSize = kit.getScreenSize(); //��ȡ��Ļ�ĳߴ�
        int screenWidth = screenSize.width; //��ȡ��Ļ�Ŀ�
        int screenHeight = screenSize.height; //��ȡ��Ļ�ĸ�
        //�̶����������
        this.setLocation(screenWidth/2-windowWidth/2, screenHeight/2-windowHeight/2);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
		// ����������¼�
		sendMessage.addActionListener(new ButtonListener());
		
		setVisible(true);
 
		// ����Ϣ�����json���ݸ�ʽ
		JSONObject data = new JSONObject();
		data.put("username", username);
		data.put("msg", null);
 
		try {
			//����һ��socket���ӷ�����
			Socket socket = new Socket(InetAddress.getLocalHost(), 8080);
 
			//��ȡ������������
			message_from_Server = new DataInputStream(socket.getInputStream());
 
			//���������������
			message_to_Server = new DataOutputStream(socket.getOutputStream());
 
			//����������� �û���
			message_to_Server.writeUTF(data.toString());
 
			//����һ���̣߳����ڶ�ȡ���������͹���������
			ReadThread readThread = new ReadThread();
			readThread.start();
 
		} catch (IOException ex) {
			//�����쳣�����ӷ�����ʧ��
			showMessage.append("No response from server");
		}
	}
 
	// �����������Ϣ�¼���
	private class ButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			try {
				//�����ж��Ƿ��߳�
				if (isKick) {
					showMessage.append("You've been kicked out\n");
				} else {
					// �������ڸ�ʽ
					SimpleDateFormat data = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// �������ڸ�ʽ
					String time = data.format(new Date()).toString();
 
					// ��ȡ�������Ϣ
					String msg = sendMessage.getText().trim();
 
					//������Ϣ����
					if (msg.equals("")) {
						//��ʾ������Ϣ����Ϊ�ո�
						JOptionPane.showMessageDialog(null, "The message can't be null", "Warning!!!", JOptionPane.ERROR_MESSAGE);
					}else if (msg.equals("STOP")) {
						//��ʾ������Ϣ����ΪSTOP����ֹ�����������쳣�˳�
						JOptionPane.showMessageDialog(null, "The message can't be STOP", "Warning!!!", JOptionPane.ERROR_MESSAGE);
					}else if(msg.equals("EXIT")) {
						// ������ݳ�json��ʽ
						JSONObject data_send = new JSONObject();
						data_send.put("username", username);
						data_send.put("msg", msg);
						data_send.put("time", time);
						// ���û�������isPrivChat���ÿ�ֵ
						data_send.put("isPrivChat", "");
						// ���������������
						message_to_Server.writeUTF(data_send.toString());
						System.exit(EXIT_ON_CLOSE);
					}else {
						//����Ϣ���Ϊ�������ݺ��û���
						String[] msg1 = msg.split("-");
						//����Ⱥ�Ļ���˽��
						if(msg1.length == 2) {
							//������ݳ�json��ʽ
							JSONObject data_send = new JSONObject();
							data_send.put("username", username);
							data_send.put("msg", msg1[1]);
							data_send.put("time", time);
							// ˽�ģ���isPrivChat���ó��û���
							data_send.put("isPrivChat", msg1[0]);
							// ���������������
							message_to_Server.writeUTF(data_send.toString());
						}else {
							// ������ݳ�json��ʽ
							JSONObject data_send = new JSONObject();
							data_send.put("username", username);
							data_send.put("msg", msg1[0]);
							data_send.put("time", time);
							// ���û�������isPrivChat���ÿ�ֵ
							data_send.put("isPrivChat", "");
							// ���������������
							message_to_Server.writeUTF(data_send.toString());
						}
					}
				}
				//���������Ϊ��
				sendMessage.setText("");
			} catch (Exception ex) {
				System.err.println(ex);
			}
		}
	}
	
	// ��ȡ������������Ϣ���߳���
	public class ReadThread extends Thread {
 
		public void run() {
			String json = null;
			try {
				// ����ѭ����������������������
				while (true) {
					// ��ȡ������������
					json = message_from_Server.readUTF();
					// ת����json��ʽ
					JSONObject data = JSONObject.fromObject(json.toString());
 
					if (json != null) {
						String mString = data.getString("msg");
 
						// �Ƿ��߳�Ⱥ��
						if (mString.contains("been kicked out") && mString.contains(username)) {
							isKick = true;
							showMessage.append(username + ",You've been kicked out\n"
									+ "Client will be close in 5s");
							Thread.sleep(5000);//��λ������
							System.exit(EXIT_ON_CLOSE);
						}else {
							// ��ӡ������Ϣ����ϵͳ��ʾ��Ϣ
							showMessage.append(mString + "\n\n");
 
							// ǿ��ʹ����ƶ���ײ�
							showMessage.selectAll();

							// ˢ���û��б�
							list.clear();
							JSONArray jsonArray = data.getJSONArray("userlist");
 
							// ��ȡ�û��б�
							for (int i = 0; i < jsonArray.size(); i++) {
								list.add(jsonArray.get(i).toString());
							}
 
							// ��ӡ�û��б�
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
