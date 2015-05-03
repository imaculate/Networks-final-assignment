import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;

public class Server {
	public static void main(String[] args) {
		ServerSocket echoServer = null;
		String line;
		DataInputStream is;
		PrintStream os;
		Socket clientSocket = null;
		
		try {
			echoServer = new ServerSocket(2222);
			
			
		} catch (IOException e) {
			System.out.println(e);
		}
		System.out.println("The server started. To stop it press <CTRL><C>.");
		
		try {
			clientSocket = echoServer.accept();
			is = new DataInputStream(clientSocket.getInputStream());
			os = new PrintStream(clientSocket.getOutputStream());
			while(true){
				line = is.readLine();
				os.println("From Server: "+line);
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}
