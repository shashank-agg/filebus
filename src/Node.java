import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.HashMap;

public class Node {
	// private String left_ip;
	// private String right_ip;
	private String group_ip = "224.0.0.3";
	private int port = 25000;
	private InetAddress group;

	public String left_ip = "", right_ip = "";

	public Node(int port_param) {
		port = port_param;
		try {
			group = InetAddress.getByName(group_ip);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		StartListening();
		SendJoinMessage();

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

	private void SendMessage(byte[] message_to_send, String ip) {
		try {
			DatagramSocket datagramSocket = new DatagramSocket();
			InetAddress address = InetAddress.getByName(ip);
			DatagramPacket packet = new DatagramPacket(message_to_send,
					message_to_send.length, address, 25000);
			// send join message
			datagramSocket.send(packet);
			System.out.println("message sent");
			datagramSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void SendJoinMessage() {
		try {

			HashMap<String, String> hash = new HashMap<String, String>();
			// prepare join message
			hash.put("title", "JOIN");
			hash.put("ip", InetAddress.getLocalHost().getHostAddress());
			SendMessage(HashToByte(hash), this.group_ip);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void SendRightJoinMessage(String ip) {
		try {
			HashMap<String, String> hash = new HashMap<String, String>();
			// prepare join message
			hash.put("title", "RJOIN");
			hash.put("ip", InetAddress.getLocalHost().getHostAddress());
			SendMessage(HashToByte(hash), ip);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void SendLeftJoinMessage(String ip) {
		try {
			HashMap<String, String> hash = new HashMap<String, String>();
			// prepare join message
			hash.put("title", "LJOIN");
			hash.put("ip", InetAddress.getLocalHost().getHostAddress());
			SendMessage(HashToByte(hash), ip);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void StartListening() {
		// start message listening thread
		Thread listenThread = new Thread(new Runnable() {
			public void run() {
				backgroundListener();
			}
		});

		listenThread.start();
	}

	private void backgroundListener() {
		HashMap<String, String> hash = new HashMap<String, String>();
		ObjectInputStream oin;
		MulticastSocket listen_socket = null;
		try {

			listen_socket = new MulticastSocket(this.port);
			listen_socket.joinGroup(group);

			byte[] received_msg = new byte[10000];
			DatagramPacket received_packet = new DatagramPacket(received_msg,
					received_msg.length);
			while (true) {
				System.out.println("Waiting for message");
				listen_socket.receive(received_packet);
				received_msg = received_packet.getData();
				ByteArrayInputStream byte_input = new ByteArrayInputStream(
						received_msg);
				oin = new ObjectInputStream(byte_input);
				hash = (HashMap<String, String>) oin.readObject();
				System.out.println("Message received from " + hash.get("ip"));

				oin.close();
				demuxer(hash);
			}
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			listen_socket.close();
		}
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
		default:
			break;
		}
	}

	private void handle_join(HashMap<String, String> hash) {
		try {
			long otherIP = ipToLong(hash.get("ip"));
			long myIP = ipToLong(InetAddress.getLocalHost().getHostAddress());
			long rightIP = ipToLong(right_ip);
			long leftIP = ipToLong(left_ip);
			
			if(rightIP == 0 || rightIP == 0)
				return;
			
			if (otherIP > myIP	&& otherIP < rightIP) {
				// Received guy is new right neighbour
				System.out.println("yes1");
				SendRightJoinMessage(hash.get("ip"));
			} 
			
			else if (otherIP < myIP	&& otherIP > leftIP) {
				// Received guy is new left neighbour
				System.out.println("yes2");
				SendLeftJoinMessage(hash.get("ip"));
			}
			else{
				System.out.println("nope");
				return;
			}
	
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	private void handle_rjoin(HashMap<String, String> hash) {
		//received permission to be right neighbour
		this.left_ip = hash.get("ip");		
		return;
	}

	private void handle_ljoin(HashMap<String, String> hash) {
		return;
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


}
