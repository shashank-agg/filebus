import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;

public class Node {
	// private String left_ip;
	// private String right_ip;
	private String group_ip = "225.4.5.6";
	public int port = 25000;
	private InetAddress group;
	public String left_ip = "", right_ip = "";
	public String left_port="25000", right_port="25000";
	public String my_ip = "";
	int multicastport;
	public Home frontend;
	
	public Node(Home frontend_param,int port_param) {
		this.port = port_param;
		this.frontend = frontend_param;
		try {
			group = InetAddress.getByName(group_ip);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		Scanner in = new Scanner(System.in);
//		System.out.println(" Enter multicast port");
		multicastport = 25000;
//		multicastport = Integer.parseInt(in.nextLine());		
		
	
		
		GUI_show_ips();
		start_ping_thread();
		  
	}

	private void start_ping_thread(){
		Thread pingThread = new Thread(new Runnable() {
			public void run() {
				pinger();
			}
		});
		pingThread.start();
	}
	
	
	private void pinger(){	
		while(true){
			try {
				leftRightPinger(this.left_ip, this.left_port);
				leftRightPinger(this.right_ip, this.right_port);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				this.frontend.statusLabel.setText("Timeout");
				SendJoinMessage();
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				e1.printStackTrace();
			}
			
			try {
				Thread.sleep(30000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void leftRightPinger (String ipToPing, String portToPing) throws IOException{
//		System.out.println("pinging "+ipToPing+":"+portToPing);
		if(ipToPing.equals(""))
			return;
		DatagramSocket datagramSocket;
		try {
			datagramSocket = new DatagramSocket(null);
			InetSocketAddress my_address = new InetSocketAddress(this.my_ip,0);
			datagramSocket.bind(my_address);
			datagramSocket.setSoTimeout(4000);
			
			HashMap<String, String> hash = new HashMap<String, String>();
			hash.put("title", "PING");
			hash.put("ip", this.my_ip);
			hash.put("port", String.valueOf(datagramSocket.getLocalPort()));
			SendMessage(HashToByte(hash), ipToPing, String.valueOf(portToPing));
			byte[] received_msg = new byte[10000];
			DatagramPacket received_packet = new DatagramPacket(received_msg,received_msg.length);
			datagramSocket.receive(received_packet);
			this.frontend.statusLabel.setText("Pong received from : "+ received_packet.getSocketAddress().toString());
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	private void send_pong(HashMap<String, String> hash_revceived){
		HashMap<String, String> hash = new HashMap<String, String>();
		hash.put("title", "PONG");
		hash.put("ip", this.my_ip);
		hash.put("port", String.valueOf(this.port));
		SendMessage(HashToByte(hash), hash_revceived.get("ip"), hash_revceived.get("port"));
	}
	
	public void StartListening() {
		// start message listening thread
		Thread listenThread = new Thread(new Runnable() {
			public void run() {
				backgroundListener();
			}
		});
		listenThread.start();
	}

	private void backgroundListener() {
		System.out.println("started listener on ip " + this.my_ip + ":"+String.valueOf(this.port));
		HashMap<String, String> hash = new HashMap<String, String>();
		ObjectInputStream oin;
		MulticastSocket listen_socket = null;
		try {

			listen_socket = new MulticastSocket(this.port);
			listen_socket.setInterface(InetAddress.getByName(this.my_ip));
			listen_socket.joinGroup(group);

			byte[] received_msg = new byte[10000];
			DatagramPacket received_packet = new DatagramPacket(received_msg,
					received_msg.length);
			while (true) {
//				System.out.println("Waiting for message on port "+ String.valueOf(this.port));
				listen_socket.receive(received_packet);
				received_msg = received_packet.getData();
				ByteArrayInputStream byte_input = new ByteArrayInputStream(
						received_msg);
				oin = new ObjectInputStream(byte_input);
				hash = (HashMap<String, String>) oin.readObject();

				this.frontend.statusLabel.setText("Message received.Type: " + hash.get("title")+" from IP: "+hash.get("ip"));
//				System.out.println("Message received.Type: " + hash.get("title")+" from IP: "+hash.get("ip"));
				oin.close();
				demuxer(hash);
			}
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			listen_socket.close();
		}
	}
	
	private void SendMessage(byte[] message_to_send, String ip,String port) {
		try {
			DatagramSocket datagramSocket = new DatagramSocket(null);
			InetSocketAddress my_address = new InetSocketAddress(this.my_ip,0); 
			datagramSocket.bind(my_address);
			InetAddress address = InetAddress.getByName(ip);
			DatagramPacket packet = new DatagramPacket(message_to_send,
					message_to_send.length, address, Integer.parseInt(port));
			// send join message
			datagramSocket.send(packet);
			datagramSocket.close();
			//System.out.println("Message sent to "+ ip + ":"+port);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void SendJoinMessage() {
		HashMap<String, String> hash = new HashMap<String, String>();
		// prepare join message
		hash.put("title", "JOIN");
		hash.put("ip", this.my_ip);
		hash.put("port", String.valueOf(this.port));
		SendMessage(HashToByte(hash), this.group_ip,String.valueOf(this.multicastport));
//		System.out.println("Sent join to "+String.valueOf(this.port));
	}

	private void SendRightJoinMessage(String ip,String port) {
		HashMap<String, String> hash = new HashMap<String, String>();
		// prepare join message
		hash.put("title", "RJOIN");
		hash.put("ip", this.my_ip);
		hash.put("port", String.valueOf(this.port));
		SendMessage(HashToByte(hash), ip, port);
	}

	private void SendLeftJoinMessage(String ip, String port) {
		HashMap<String, String> hash = new HashMap<String, String>();
		// prepare join message
		hash.put("title", "LJOIN");
		hash.put("ip", this.my_ip);
		hash.put("port", String.valueOf(this.port));
		SendMessage(HashToByte(hash), ip,port);
	}

	

	private void demuxer(HashMap<String, String> hash) {
		switch (hash.get("title")) {
		case "JOIN": // Received request for join
			handle_join(hash);
			break;
		case "RJOIN": // Received permission to be right neighbour
			handle_rjoin(hash);
			break;
		case "LJOIN": // Received permission to be right neighbour
			handle_ljoin(hash);
			break;
		case "RSRCH": //Received search request from left neighbor
			search_right(hash);
			break;
		case "LSRCH": //Received search request from right neighbor
			search_left(hash);
			break;
		case "RESLT": //found match for search parameter.
			GUI_update_result(hash);
			break;
		case "PING":
			send_pong(hash);
		default:
			break;
		}
	}

	private void handle_join(HashMap<String, String> hash) {
		long otherIP = ipToLong(hash.get("ip"));
		long myIP = ipToLong(this.my_ip);
		long rightIP = ipToLong(right_ip);
		long leftIP = ipToLong(left_ip);

		if (otherIP >= myIP	&& ((otherIP <= rightIP) || (rightIP == 0)) && !(this.port == Integer.parseInt(hash.get("port")) && myIP == otherIP)) {
			// Received guy is new right neighbour
			this.right_ip = hash.get("ip");
			this.right_port = hash.get("port");
			SendRightJoinMessage(hash.get("ip"),hash.get("port"));
		} 
		
		else if (otherIP < myIP	&& ((otherIP > leftIP) || (leftIP == 0))) {
			// Received guy is new left neighbour
			this.left_ip= hash.get("ip");
			this.left_port = hash.get("port");
			SendLeftJoinMessage(hash.get("ip"),hash.get("port"));
		}
		else{
			return;
		}
	}

	private void handle_rjoin(HashMap<String, String> hash) {
		//received permission to be right neighbour
		
		this.left_ip = hash.get("ip");
		this.left_port = hash.get("port");

		return;
	}

	private void handle_ljoin(HashMap<String, String> hash) {
		this.right_ip = hash.get("ip");
		this.right_port = hash.get("port");

		return;
	}
	
	public void searchFile(String searchKey)
	{
		HashMap<String, String> hash = new HashMap<String, String>();
		// prepare join message
		hash.put("title", "LSRCH");
		hash.put("ip", this.my_ip);
		hash.put("port", String.valueOf(this.port));
		hash.put("key", searchKey);
		if(!this.left_ip.equals(""))
		SendMessage(HashToByte(hash), this.left_ip,this.left_port);
		hash.put("title", "RSRCH");
		if(!this.right_ip.equals(""))
		SendMessage(HashToByte(hash), this.right_ip,this.right_port);
		
	}
	
	private void search_left(HashMap<String, String> hash)
	{
		//long sourceIP = ipToLong(hash.get("ip"));
		//long myIP = ipToLong(this.my_ip);
		long leftIP = ipToLong(left_ip);
		
		
		HashMap<String, String> hash2  = search(hash.get("key"));
		if(hash2.size()>0)
		{
				//match found.
				//return the result to called IP.
			// prepare found message
			hash2.put("title", "RESLT");
			hash2.put("ip", this.my_ip);
			hash2.put("port", String.valueOf(this.port));
			SendMessage(HashToByte(hash2), hash.get("ip"),hash.get("port"));
		}
		//call search_left for left of left node
		if(leftIP==0)
			return;
		SendMessage(HashToByte(hash), this.left_ip,this.left_port);
		
	}

	private void search_right(HashMap<String, String> hash)
	{
		//long sourceIP = ipToLong(hash.get("ip"));
		//long myIP = ipToLong(this.my_ip);
		long rightIP = ipToLong(right_ip);
		HashMap<String, String> hash2  = search(hash.get("key"));

		if(hash2.size()>0)
		{
				//match found.
				//return the result to called IP.
			// prepare found message
			hash2.put("title", "RESLT");
			hash2.put("ip", this.my_ip);
			hash2.put("port", String.valueOf(this.port));
			SendMessage(HashToByte(hash2), hash.get("ip"),hash.get("port"));

		}
		//call search_right for right of right node
		if(rightIP==0)
			return;
		SendMessage(HashToByte(hash), this.right_ip,this.right_port);
			
	}

	private HashMap<String, String> search(String searchKey)
	{
		//makes a list of all files in C drive

		File dir = new File("C:/Users/TANVI");
		String[] children = dir.list();
		int k=0;
		HashMap<String, String> hash = new HashMap<String, String>();
		for(int i = 0;i<children.length;i++)
		{
			boolean retval = children[i].toLowerCase().contains(searchKey.toLowerCase());
		    if(retval == true)
		    {
		    	hash.put(String.valueOf(k),children[i]);
		    	k++;
		    }
		}
		return hash;
	}

	private byte[] HashToByte(HashMap<String, String> hash) {
		try {
			ByteArrayOutputStream byteout = new ByteArrayOutputStream();
			ObjectOutputStream oout = new ObjectOutputStream(byteout);
			oout.writeObject(hash);
			oout.flush();
			oout.close();
			byte[] serialized_msg = byteout.toByteArray();
			return serialized_msg;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public long ipToLong(String ipAddress) {
		if(ipAddress.equals("")) //in case left or right ip is empty
			return 0;		
		
		String[] ipAddressInArray = ipAddress.split("\\.");	 
		long result = 0;
		for (int i = 0; i < ipAddressInArray.length; i++) {	 
			int power = 3 - i;
			int ip = Integer.parseInt(ipAddressInArray[i]);
			result += ip * Math.pow(256, power);	 
		}
	 
		return result;
	  }
	
	private void GUI_update_result(HashMap<String, String> hash)
	{
		DefaultListModel model = new DefaultListModel();
		for(int i=0;i<(hash.size())-2;i++)
		{
			model.addElement((hash.get(String.valueOf(i)) + " : " + hash.get("ip")));
		}
		this.frontend.resultList.setModel(model);
	}
	
	private void GUI_show_ips(){
		try {
			InetAddress localhost;
			localhost = InetAddress.getLocalHost();
			DefaultListModel model1 = new DefaultListModel();
			InetAddress[] allMyIps = InetAddress.getAllByName(localhost.getCanonicalHostName());
			if (allMyIps != null && allMyIps.length >= 1) {
			    for (int i = 0; i < allMyIps.length; i++) {
			      model1.addElement(allMyIps[i].getHostAddress());
			    }
			    this.frontend.ipList.setModel(model1);
			  }
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
