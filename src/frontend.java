import java.util.Scanner;


public class frontend {
	public static void main(String[] args){
	String choice;
	
	System.out.println(" 1.Run as server \n 2.Run as client\n 3.Exit");
	Scanner in = new Scanner(System.in);
	while(true){
	choice = in.nextLine();
	if(choice.equals("1")){
		Server server = new Server();
	}
	else if (choice.equals("2")) {
		Client client = new Client();
	}
	else{
		break;
	}
	}
	}
}
