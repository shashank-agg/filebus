import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;


public class Client {
	//private String left_ip;
	//private String right_ip;
	private String group_ip = "224.0.0.3";
	private int port = 25000;
	private InetAddress group;
	public Client(){
		try {
			DatagramSocket join_socket = new DatagramSocket();
			HashMap<String,String> hash= new HashMap<String,String>();
			
			//prepare join message
			hash.put("title", "JOIN");
			hash.put("ip",InetAddress.getLocalHost().getHostAddress());
			ByteArrayOutputStream byteout = new ByteArrayOutputStream();
			ObjectOutputStream oout = new ObjectOutputStream(byteout);
			oout.writeObject(hash);
			oout.flush();
			oout.close();
			
			byte[] serialized_msg = byteout.toByteArray();
			group = InetAddress.getByName(group_ip);
			DatagramPacket packet = new DatagramPacket(serialized_msg, serialized_msg.length,group,port);
			
			//send join message
			join_socket.send(packet);
			System.out.println("message sent " + packet.getData().toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
