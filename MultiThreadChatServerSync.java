
import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;


public class MultiThreadChatServerSync {
	private static ServerSocket serverSocket = null;
	private static Socket clientSocket = null;
	private static final int maxClientCount = 10;
	private static final clientThread[] threads = new clientThread[maxClientCount];
	
	public static void main(String[] args) {
		int portNumber = 2222;
		if(args.length<1){
			System.out.println("Usage: java MultiThreadChatServerSync <portNumber>\n" + 
		"Now using portNumber = " + portNumber);
		}
		else{
			portNumber = Integer.valueOf(args[0]).intValue();
		}
		
		try {
			serverSocket = new ServerSocket(portNumber);
		} catch (Exception e) {
			System.out.println(e);
		}
		
		while(true){
			try {
				clientSocket = serverSocket.accept();
				int i=0;
				for(i=0;i<maxClientCount;i++){
					if(threads[i]==null){
						(threads[i] = new clientThread(clientSocket,threads)).start();
						break;
					}
				}
				if (i==maxClientCount){
					PrintStream os = new PrintStream(clientSocket.getOutputStream());
					os.println("Server too busy. Try later.");
					os.close();
					clientSocket.close();
				}
			} catch (IOException e) {
				System.out.println(e);
			}
		}
	}
}

class clientThread extends Thread{
	private DataInputStream is=null;
	private PrintStream os = null;
	private Socket clientSocket =null;
	private final clientThread[] threads;
	private int maxClientCount;
	private String clientName = null;
	public clientThread(Socket clientSocket, clientThread[] threads) {
		this.clientSocket = clientSocket;
		this.threads = threads;
		maxClientCount = threads.length;
	} 
	@Override
	public void run() {
			int maxClientsCount = this.maxClientCount;
			clientThread[] threads = this.threads;
			
			
			try{
				is = new DataInputStream(clientSocket.getInputStream());
				os = new PrintStream(clientSocket.getOutputStream());
				String name;
				
				while(true){
					os.println("Enter your name.");
					name= is.readLine().trim();
					if(name.indexOf('@')==-1)
						break;
					else
							os.println("The name should not contain '@' character.");
				}
				os.println("Welcome "+ name+" to our chat room.\nTo leave enter /quit in a new line");
				synchronized (this) {
						
					for(int i=0; i<maxClientsCount;i++){
						if(threads[i]!=null && threads[i]==this){
							clientName = "@"+name;
							break;
						}
					}
					for(int i=0; i<maxClientsCount;i++){
						if(threads[i]!=null && threads[i]!=this){
							threads[i].os.println("*** A new user "+name+" entered the chat room !!! ****");
						}
					}
				}
				while(true){
					String line = is.readLine();
					if(line.startsWith("/quit")){
						break;
					}
					if(line.startsWith("@")){
						String[] words=line.split("\\s",2);
						if(words.length>1 && words[1]!=null){
							words[1] = words[1].trim();
							if(!words[1].isEmpty()){
								synchronized (this) {
									for(int i=0; i<maxClientsCount;i++){
										if (threads[i]!=null && threads[i]!=this && threads[i].clientName!=null && threads[i].clientName.equals(words[0])){
											threads[i].os.println("<" +name + "> "+ words[1]);
											os.println("<"+name+"> " +words[1]);
											break;
										}
									}
								}
							}
						}
					}
					else{
						synchronized (this) {
							for(int i=0; i< maxClientsCount;i++){
								if(threads[i]!=null && threads[i].clientName!=null){
									threads[i].os.println("<"+name+"> "+ line); 
								}
							}
						}	
					}
					
				}
				synchronized (this) {
					for(int i=0;i<maxClientsCount;i++){
						if(threads[i] != null && threads[i]!=this && threads[i].clientName!=null){
							threads[i].os.println("*** The user "+name+" is leaving the chat room !!! ***");
						}
					}
				}
				
				os.println("*** Bye " + name + " ***");
				synchronized (this) {
					for(int i=0; i<maxClientsCount;i++){
						if(threads[i]==this){
							threads[i]=null;
						}
					}
				}
				
				is.close();
				os.close();
				clientSocket.close();
			}
			catch(IOException e){}
	}
	
}
