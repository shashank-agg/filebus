
import java.util.Scanner;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.JButton;
import javax.swing.JTabbedPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.GridLayout;
import javax.swing.JMenuBar;
import javax.swing.border.BevelBorder;
import javax.swing.border.MatteBorder;
import java.awt.Color;
import javax.swing.border.EtchedBorder;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;


public class Home extends JFrame {
	public JPanel contentPane;
	public final JTextPane textPane = new JTextPane();
	private Node backend;
	public JTextField fileName;
	public JScrollPane scrollPane;
	public JList resultList;
	public JList ipList;
	public JLabel statusLabel;
	public JButton getFileButton;
	private JButton searchButton;
	
	public static void main(String[] args){
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Throwable e) {
			e.printStackTrace();
		}
		Home frame = new Home();
		frame.setVisible(true);
		
		System.out.println(" Any key to exit");
		Scanner in = new Scanner(System.in);
		in.nextLine();
	}
	
	
	public Home() {
		String choice;	
		Scanner in = new Scanner(System.in);
		System.out.println(" Enter port");
		int port = 25000;
		port = Integer.parseInt(in.nextLine());
		
		
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 600, 450);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Enter file name:");
		lblNewLabel.setBounds(10, 11, 117, 14);
		contentPane.add(lblNewLabel);
		
		fileName = new JTextField();
		fileName.setBounds(10, 36, 384, 20);
		contentPane.add(fileName);
		fileName.setColumns(10);
		
		JLabel lblNewLabel_1 = new JLabel("Results:");
		lblNewLabel_1.setBounds(10, 67, 46, 14);
		contentPane.add(lblNewLabel_1);
		
		searchButton = new JButton("Search");
		
		searchButton.setBounds(415, 35, 139, 23);
		contentPane.add(searchButton);
		
		getFileButton = new JButton("Get this file");
		getFileButton.setBounds(10, 327, 89, 23);
		contentPane.add(getFileButton);
		
		scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 92, 384, 224);
		contentPane.add(scrollPane);
		
		resultList = new JList();
		resultList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrollPane.setViewportView(resultList);
		
		JButton useIpButton = new JButton("Use this IP");
		
		useIpButton.setBounds(415, 327, 139, 23);
		contentPane.add(useIpButton);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setViewportBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		scrollPane_1.setBounds(415, 92, 141, 224);
		contentPane.add(scrollPane_1);
		
		ipList = new JList();
		ipList.setSelectedIndex(0);
		ipList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrollPane_1.setViewportView(ipList);
		
		JLabel lblAvailableIps = new JLabel("Available IP's:");
		scrollPane_1.setColumnHeaderView(lblAvailableIps);
		
		JLabel lblNewLabel_2 = new JLabel("STATUS:");
		lblNewLabel_2.setBounds(10, 377, 46, 23);
		contentPane.add(lblNewLabel_2);
		
		statusLabel = new JLabel("");
		statusLabel.setBounds(66, 377, 488, 23);
		contentPane.add(statusLabel);
		
		
		// now attach backend
		this.backend = new Node(this,port);
		
		
		useIpButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(ipList.getSelectedValue() == null)
					return;
				backend.my_ip = ipList.getSelectedValue().toString();
				backend.StartListening();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				backend.SendJoinMessage();
			}
		});
		
		searchButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				backend.searchFile(fileName.getText());
			}
		});

	}
}

