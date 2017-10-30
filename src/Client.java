import java.net.*;
import java.io.*;
import java.util.*;


public class Client  {

	
	private ObjectInputStream in_socket;		
	private ObjectOutputStream out_socket;		
	private Socket socket;

	
	private ClientGUI clientGUI;
	
	
	private String server, username;
	private int port;

	
	Client(String server, int port, String username) {
		this(server, port, username, null);
	}

	
	Client(String server, int port, String username, ClientGUI cg) {
		this.server = server;
		this.port = port;
		this.username = username;
		// save if we are in GUI mode or not
		this.clientGUI = cg;
	}
	
	
	public boolean start() {
		try {
			socket = new Socket(server, port);
		} 
		catch(Exception ec) {
			display("Error connectiong to server:" + ec);
			return false;
		}
		
		String msg = "Connection accepted " + socket.getInetAddress() + ":" + socket.getPort();
		display(msg);
	
		try
		{
			in_socket  = new ObjectInputStream(socket.getInputStream());
			out_socket = new ObjectOutputStream(socket.getOutputStream());
		}
		catch (IOException eIO) {
			display("Exception creating new Input/output Streams: " + eIO);
			return false;
		}

		
		new ListenFromServer().start();
		try
		{
			out_socket.writeObject(username);
		}
		catch (IOException eIO) {
			display("Exception doing login : " + eIO);
			disconnect();
			return false;
		}
		
		return true;
	}

	private void display(String msg) {
		if(clientGUI == null)
			System.out.println(msg);      
		else
			clientGUI.append(msg + "\n");	
	}
	

	void sendMessage(ChatMessage msg) {
		try {
			out_socket.writeObject(msg);
		}
		catch(IOException e) {
			display("Exception writing to server: " + e);
		}
	}

	
	private void disconnect() {
		try { 
			if(in_socket != null) in_socket.close();
		}
		catch(Exception e) {} 
		try {
			if(out_socket != null) out_socket.close();
		}
		catch(Exception e) {} 
        try{
			if(socket != null) socket.close();
		}
		catch(Exception e) {} 
		
		
		if(clientGUI != null)
			clientGUI.connectionFailed();
			
	}
	/*
	 * 
	 * > java Client
	 * > java Client username
	 * > java Client username portNumber
	 * > java Client username portNumber serverAddress
	 * 
	 */
	public static void main(String[] args) {
		int portNumber = 1500;
		String serverAddress = "localhost";
		String userName = "Anonymous";

		
		switch(args.length) {
		
			case 3:
				serverAddress = args[2];
			case 2:
				try {
					portNumber = Integer.parseInt(args[1]);
				}
				catch(Exception e) {
					System.out.println("Invalid port number.");
					System.out.println("Usage is: > java Client [username] [portNumber] [serverAddress]");
					return;
				}
			
			case 1: 
				userName = args[0];
			
			case 0:
				break;
			
			default:
				System.out.println("Usage is: > java Client [username] [portNumber] {serverAddress]");
			return;
		}
		
		Client client = new Client(serverAddress, portNumber, userName);
		
		if(!client.start())
			return;
		
	
		Scanner scan = new Scanner(System.in);
		
		while(true) {
			System.out.print("> ");
			
			String msg = scan.nextLine();
			
			if(msg.equalsIgnoreCase("LOGOUT")) {
				client.sendMessage(new ChatMessage(ChatMessage.LOGOUT, ""));
				
				break;
			}
		
			else if(msg.equalsIgnoreCase("WHOISIN")) {
				client.sendMessage(new ChatMessage(ChatMessage.WHOISIN, ""));				
			}
			else {				
				client.sendMessage(new ChatMessage(ChatMessage.MESSAGE, msg));
			}
		}
		
		client.disconnect();	
	}

	class ListenFromServer extends Thread {

		public void run() {
			while(true) {
				try {
					String msg = (String) in_socket.readObject();
					if(clientGUI == null) {
						System.out.println(msg);
						System.out.print("> ");
					}
					else {
						clientGUI.append(msg);
					}
				}
				catch(IOException e) {
					display("Server has close the connection: " + e);
					if(clientGUI != null) 
						clientGUI.connectionFailed();
					break;
				}
				
				catch(ClassNotFoundException e2) {
				}
			}
		}
	}
}
