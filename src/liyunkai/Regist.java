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
 * 用户输入用户名并注册
 * 通过启动这个程序进入客户端
 * @param username: 用户名，不能为空并且不能为STOP
 * 基于JFrame实现图形界面
 */

public class Regist extends JFrame {
 
	public static void main(String[] args) {
		new Regist();
	}
 
	public Regist() {
		
		/**
		 * 界面
		 * 
		 * 使用Borderlayout布局
		 * @param name_input: 注册界面的输入框，用于接收用户的输入
		 * @param hint_text: 提示用户进行输入的文本框
		 */
		
		//用户名输入框
		JTextField name_input = new JTextField(1);
		name_input.setFont(new Font("", Font.BOLD, 20)); 
		name_input.setHorizontalAlignment(JTextField.CENTER); //输入从中间

		//提示
		JLabel hint_text = new JLabel("Enter username (press ENTER to login)", JLabel.CENTER);
		hint_text.setFont(new Font("", Font.BOLD, 17));
		
		//logo
		JLabel hint_blank1 = new JLabel();
		Icon icon = new ImageIcon("res/logo.jpg");
		hint_blank1.setIcon(icon);
		hint_blank1.setVerticalTextPosition(JLabel.BOTTOM); //和下面的一起将图片放置于框的正中间
		hint_blank1.setHorizontalTextPosition(JLabel.CENTER);
		hint_blank1.setText("Click picture to know about author");
		hint_blank1.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
			}
			
			@Override
			//添加鼠标长按图片事件
			public void mousePressed(MouseEvent e) {
			}
			
			@Override
			public void mouseExited(MouseEvent e) {				
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {				
			}
			
			@Override
			//添加鼠标点击图片事件
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
		
		//整体窗口
		JPanel p1 = new JPanel();
		p1.setLayout(new BorderLayout());
		p1.add(hint_text, BorderLayout.CENTER); //将提示框放置在中间
		p1.add(name_input, BorderLayout.SOUTH); //将输入框放置在最下面
		p1.add(hint_blank1, BorderLayout.WEST);
		
		this.add(p1, BorderLayout.CENTER);
		this.setTitle("Welcome to login"); //标题
		this.setSize(600, 300); //尺寸
		
		int windowWidth = this.getWidth(); //获得窗口宽
        int windowHeight = this.getHeight();//获得窗口高
        Toolkit kit = Toolkit.getDefaultToolkit(); //定义工具包
        Dimension screenSize = kit.getScreenSize(); //获取屏幕的尺寸
        int screenWidth = screenSize.width; //获取屏幕的宽
        int screenHeight = screenSize.height; //获取屏幕的高
        //固定将界面居中
        this.setLocation(screenWidth/2-windowWidth/2, screenHeight/2-windowHeight/2);
        
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //关闭按钮
		//设置窗口大小不可变
        this.setResizable(false);
        this.setVisible(true);
 
		// 回车登陆
		name_input.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// 获取用户名
				String username = name_input.getText().trim();
 
				if (username.equals("")) {
					//用户名不能为空
					JOptionPane.showMessageDialog(null, "The username can't be null", "Warning!!!", JOptionPane.ERROR_MESSAGE);
				} else {
					// 关闭设置页面，启动聊天框页面
					setVisible(false);
					new Client(username);
				}
			}
		});
 
	}
}