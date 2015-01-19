import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;


public class Server {
	private String left_ip;
	private String right_ip;
	private String group_ip = "224.0.0.3";
	private int port = 25000;
	private InetAddress group;
	public Server(){
		try {
			group = InetAddress.getByName(group_ip);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		//start message listening thread
		Thread listenThread = new Thread(new Runnable() {
			public void run() {
				ListenForMessages();
			}
		});
		
		listenThread.start();
	}
	
	private void ListenForMessages(){
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
				System.out.println("Message received:"+hash.get("title"));
				oin.close();							
			}
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			listen_socket.close();
		}
	}
}
