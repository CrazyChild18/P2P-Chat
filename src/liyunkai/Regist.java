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
 * The user enters the user name and registers
 * Enter the client by launching this program
 * @param username: User name, cannot be empty and cannot be STOP or EXIT
 * Implement graphical interface based on JFrame
 */
public class Regist extends JFrame {
 
	public static void main(String[] args) {
		new Regist();
	}
 
	public Regist() {
		
		/**
		 * interface
		 * Use the Borderlayout layout
		 * @param name_input: An input box on the registration interface for receiving user input
		 * @param hint_text: A text box that prompts the user for input
		 */
		
		//User name input box
		JTextField name_input = new JTextField(1);
		name_input.setFont(new Font("", Font.BOLD, 20)); 
		name_input.setHorizontalAlignment(JTextField.CENTER); //Input from the middle

		//hint
		JLabel hint_text = new JLabel("Enter username (press ENTER to login)", JLabel.CENTER);
		hint_text.setFont(new Font("", Font.BOLD, 17));
		
		//logo
		JLabel hint_blank1 = new JLabel();
		Icon icon = new ImageIcon("res/logo.jpg");
		hint_blank1.setIcon(icon);
		hint_blank1.setVerticalTextPosition(JLabel.BOTTOM); //Place the picture in the middle of the box with the one below
		hint_blank1.setHorizontalTextPosition(JLabel.CENTER);
		hint_blank1.setText("Click picture to know about system");
		hint_blank1.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
			}
			
			@Override
			public void mouseExited(MouseEvent e) {				
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {				
			}
			
			@Override
			//Add a mouse-click image event
			public void mouseClicked(MouseEvent e) {
				JOptionPane.showMessageDialog(null, "@Author: Li Yunkai\n"
						+ "@Email: 2049721941@qq.com\n"
						+ "Welcome to use this P2P chat system\n"
						+ "If the server comes online, then chat\n"
						+ "Enter the message in input text area below\n"
						+ "If you want send message privatly\n"
						+ "Just add userId- before message\n"
						+ "Also you can enter userId-STATS to check the common that the user use\n"
						+ "If you want exit, then input EXIT\n"
						+ "Have a nice try!",
					"ABOUT", JOptionPane.DEFAULT_OPTION);
			}
		});
		
		JPanel p1 = new JPanel();
		p1.setLayout(new BorderLayout());
		p1.add(hint_text, BorderLayout.CENTER); //Place the prompt box in the middle
		p1.add(name_input, BorderLayout.SOUTH); //Place the input field at the bottom
		p1.add(hint_blank1, BorderLayout.WEST);
		
		this.add(p1, BorderLayout.CENTER);
		this.setTitle("Welcome to login"); //Title
		this.setSize(600, 300); //Size
		
		int windowWidth = this.getWidth(); //Get window width
        int windowHeight = this.getHeight();//Get window height
        Toolkit kit = Toolkit.getDefaultToolkit(); //Definition toolkit
        Dimension screenSize = kit.getScreenSize(); //Gets the screen size
        int screenWidth = screenSize.width; //Gets the width of the screen
        int screenHeight = screenSize.height; //Gets the height of the screen
        //Fix and center the interface
        this.setLocation(screenWidth/2-windowWidth/2, screenHeight/2-windowHeight/2);
        
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//Set the window size to be immutable
        this.setResizable(false);
        this.setVisible(true);
 
		// "Enter" to login
		name_input.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				//Get UserName
				String username = name_input.getText().trim();
 
				if (username.equals("")) {
					//The user name cannot be empty
					JOptionPane.showMessageDialog(null, "The username can't be null", "Warning!!!", JOptionPane.ERROR_MESSAGE);
				} else {
					//Close the Settings page and launch the chat box page
					setVisible(false);
					new Client(username);
				}
			}
		});
 
	}
}