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
	//private String left_ip;
	//private String right_ip;
	private String group_ip = "224.0.0.3";
	private int port = 25000;
	private InetAddress group;
	
	public String left_ip,right_ip;
	public Node(int port_param){
		port = port_param;		
		try {
			group = InetAddress.getByName(group_ip);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		SendJoinMessage();		
		StartListening();		
	}
	
	
	private void SendMessage(HashMap<String,String> hash,DatagramSocket sock){
		try {
			ByteArrayOutputStream byteout = new ByteArrayOutputStream();
			ObjectOutputStream oout = new ObjectOutputStream(byteout);
			oout.writeObject(hash);
			oout.flush();
			oout.close();
			
			byte[] serialized_msg = byteout.toByteArray();
			
			DatagramPacket packet = new DatagramPacket(serialized_msg, serialized_msg.length,group,25000);
			
			//send join message
			sock.send(packet);
			System.out.println("message sent of type" + hash.get("title"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	private void SendJoinMessage() {
		try {
			DatagramSocket join_socket = new DatagramSocket();
			HashMap<String,String> hash= new HashMap<String,String>();
			
			//prepare join message
			hash.put("title", "JOIN");
			hash.put("ip",InetAddress.getLocalHost().getHostAddress());
			SendMessage(hash, join_socket);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	private void StartListening() {
		//start message listening thread
		Thread listenThread = new Thread(new Runnable() {
			public void run() {
				backgroundListener();
			}
		});
		
		listenThread.start();
	}
	
	
	private void backgroundListener(){
		HashMap<String,String> hash= new HashMap<String,String>();
		ObjectInputStream oin;
		MulticastSocket listen_socket = null;
		try {
			
			listen_socket = new MulticastSocket(this.port);
			listen_socket.joinGroup(group);
			
			byte[] received_msg = new byte[10000];
			DatagramPacket received_packet = new DatagramPacket(received_msg, received_msg.length);
			while(true){
				System.out.println("Waiting for message");
				listen_socket.receive(received_packet);
				received_msg = received_packet.getData();
				ByteArrayInputStream byte_input = new ByteArrayInputStream(received_msg);	
				oin = new ObjectInputStream(byte_input);
				hash = (HashMap<String, String>)oin.readObject();
				System.out.println("Message received from "+hash.get("ip"));
				
				oin.close();							
			}
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			listen_socket.close();
		}
	}
	
	private void demuxer(HashMap<String,String> hash){
		switch (hash.get("title")) {
		case "JOIN":
			handle_join(hash);
			break;

		default:
			break;
		}
	}
	
	private void handle_join(HashMap<String,String> hash){
		try {
			int result1 = is_greater(hash.get("ip"), InetAddress.getLocalHost().getHostAddress());
			if(result1 == 1){
				int result2 = is_greater(hash.get("ip"), right_ip);
				if(result2 == 0){
					right_ip = hash.get("ip");
				}
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	    public int is_greater(String ip1, String ip2) {
	    	
	    	if(ip1.isEmpty() || ip2.isEmpty())
	    		return -1;
	    	
	    	String[] arr1 = ip1.split(".");
	    	String[] arr2 = ip2.split(".");
	    	
	    	if(arr1.length > arr2.length)
	    		return 1;
	    	else if (arr1.length < arr2.length) 
				return 0;
			
	        // we have 2 ips of the same type, so we have to compare each byte
	        for(int i = 0; i < arr1.length; i++) {
	            int b1 = Integer.parseInt(arr1[i]);
	            int b2 = Integer.parseInt(arr2[i]);
	            if(b1 == b2)
	                continue;
	            if(b1 < b2)
	                return 0;
	            else
	                return 1;
	        }
	        return 2; //equal-not used
	    }
	 
	    private int unsignedByteToInt(byte b) {
	        return (int) b & 0xFF;
	       
	    }
	
	
}

