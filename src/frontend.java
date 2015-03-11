import java.util.Scanner;


public class frontend {
	public static void main(String[] args){
	String choice;
	
	Scanner in = new Scanner(System.in);
	System.out.println(" Enter port");
	int port = Integer.parseInt(in.nextLine());
	Node me = new Node(port);
	System.out.println(" Any key to exit");
	choice = in.nextLine();		
	}
}

