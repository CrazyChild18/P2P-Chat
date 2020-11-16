package liyunkai;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

 
/**
 * @author Eric Li
 * 
 * �û������û�����ע��
 * ͨ����������������ͻ���
 * @param username: �û���������Ϊ�ղ��Ҳ���ΪSTOP
 * ����JFrameʵ��ͼ�ν���
 */

public class Regist extends JFrame {
 
	public static void main(String[] args) {
		new Regist();
	}
 
	public Regist() {
		
		/**
		 * ����
		 * 
		 * ʹ��Borderlayout����
		 * @param name_input: ע��������������ڽ����û�������
		 * @param hint_text: ��ʾ�û�����������ı���
		 */
		
		//�û��������
		JTextField name_input = new JTextField(1);
		name_input.setFont(new Font("", Font.BOLD, 20)); 
		name_input.setHorizontalAlignment(JTextField.CENTER); //������м�

		//��ʾ
		JLabel hint_text = new JLabel("Enter username (press ENTER to login)", JLabel.CENTER);
		hint_text.setFont(new Font("", Font.BOLD, 17));
		
		//logo
		JLabel hint_blank1 = new JLabel();
		Icon icon = new ImageIcon("res/logo.jpg");
		hint_blank1.setIcon(icon);
		hint_blank1.setVerticalTextPosition(JLabel.BOTTOM); //�������һ��ͼƬ�����ڿ�����м�
		hint_blank1.setHorizontalTextPosition(JLabel.CENTER);
		hint_blank1.setText("Click picture to know about author");
		hint_blank1.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
			}
			
			@Override
			//�����곤��ͼƬ�¼�
			public void mousePressed(MouseEvent e) {
			}
			
			@Override
			public void mouseExited(MouseEvent e) {				
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {				
			}
			
			@Override
			//��������ͼƬ�¼�
			public void mouseClicked(MouseEvent e) {
				JOptionPane.showMessageDialog(null, "@Author: Li Yunkai\n"
						+ "@Email: 2049721941@qq.com\n"
						+ "Welcome to use this demo\n"
						+ "This is a P2P chat system\n"
						+ "When the server comes online\n"
						+ "Users can chat by client\n",
					"ABOUT", JOptionPane.DEFAULT_OPTION);
			}
		});
		
		//���崰��
		JPanel p1 = new JPanel();
		p1.setLayout(new BorderLayout());
		p1.add(hint_text, BorderLayout.CENTER); //����ʾ��������м�
		p1.add(name_input, BorderLayout.SOUTH); //������������������
		p1.add(hint_blank1, BorderLayout.WEST);
		
		this.add(p1, BorderLayout.CENTER);
		this.setTitle("Welcome to login"); //����
		this.setSize(600, 300); //�ߴ�
		
		int windowWidth = this.getWidth(); //��ô��ڿ�
        int windowHeight = this.getHeight();//��ô��ڸ�
        Toolkit kit = Toolkit.getDefaultToolkit(); //���幤�߰�
        Dimension screenSize = kit.getScreenSize(); //��ȡ��Ļ�ĳߴ�
        int screenWidth = screenSize.width; //��ȡ��Ļ�Ŀ�
        int screenHeight = screenSize.height; //��ȡ��Ļ�ĸ�
        //�̶����������
        this.setLocation(screenWidth/2-windowWidth/2, screenHeight/2-windowHeight/2);
        
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //�رհ�ť
		//���ô��ڴ�С���ɱ�
        this.setResizable(false);
        this.setVisible(true);
 
		// �س���½
		name_input.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// ��ȡ�û���
				String username = name_input.getText().trim();
 
				if (username.equals("")) {
					//�û�������Ϊ��
					JOptionPane.showMessageDialog(null, "The username can't be null", "Warning!!!", JOptionPane.ERROR_MESSAGE);
				} else {
					// �ر�����ҳ�棬���������ҳ��
					setVisible(false);
					new Client(username);
				}
			}
		});
 
	}
}