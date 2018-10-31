package semaphore; 

import java.io.IOException; 
import java.io.ObjectInputStream; 
import java.io.ObjectOutputStream; 
import java.net.ServerSocket; 
import java.net.Socket;  

/**
 * This thread sets up the communication network between all the 
 * <code>DisSemHelper</code>s that must coordinate to maintain the distributed 
 * semaphore. Sends them the connection information for all the nodes and 
 * then simply shuts downs, as it will not be needed anymore. 
 * @author Franklin D. Worrell
 * @version 29 November 2017
 */ 
public class Initiator implements Runnable {
	
	private String ip; 
	private int port;
	private int numberOfNodes;
	private ServerSocket serverSocket; 
	private ObjectInputStream[] ins; 
	private ObjectOutputStream[] outs; 
	private String[] ips; 
	private int[] ports; 
	
	public Initiator(String ip, int port, int numberOfNodes) { 
		this.ip = ip; 
		this.port = port;
		this.numberOfNodes = numberOfNodes; 
		this.ins = new ObjectInputStream[numberOfNodes]; 
		this.outs = new ObjectOutputStream[numberOfNodes]; 
		this.ips = new String[numberOfNodes]; 
		this.ports = new int[numberOfNodes]; 
	}
	
	/**
	 * Configures the server socket for the thread, receives the connection 
	 * information from each <code>DisSemHelper</code>, then sends this 
	 * information back out to all the <code>DisSemHelper</code>s. 
	 */ 
	@Override
	public void run() {
		this.configure(); 
		this.getAllIPsAndPortsFromDisSemHelpers(); 
		this.broadcastConnectionInfoToDisSemHelpers(); 
		this.closeUpShop(); 
	} 

	/** 
	 * Opens a server socket and accepts a connection from each of the 
	 * anticipated <code>DisSemHelper</code>s. 
	 */ 
	private void configure() {
		this.serverSocket = null; 
		try {
			System.out.println("Starting Initiator thread on " + ip + ":" + port); 
			this.serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			System.err.println("Error opening Initiator socket. Terminating."); 
			e.printStackTrace(); 
			System.exit(-1); 
		} 
		
		for (int i = 0; i < this.numberOfNodes; i++) {
			try {
				Socket client = this.serverSocket.accept(); 
				this.outs[i] = new ObjectOutputStream(client.getOutputStream()); 
				this.ins[i] = new ObjectInputStream(client.getInputStream()); 
			} catch (IOException e) {
				System.err.println("Error configuring connection to bidder."); 
				e.printStackTrace(); 
			} 
		} 
	} 
	
	/**
	 * Cycles through each input stream and gets a message containing the 
	 * connection information from the sender. 
	 */ 
	private void getAllIPsAndPortsFromDisSemHelpers() {
		for (int i = 0; i < this.numberOfNodes; i++) {
			ConnectionInfoMessage msg = null; 
			try {
				msg = (ConnectionInfoMessage) ins[i].readObject(); 
			} catch (ClassNotFoundException | IOException e) {
				System.err.println("Error receiving a DisSemHelper's connection info."); 
				e.printStackTrace(); 
				continue; 
			} 
			
			this.ips[msg.getNodeId()] = msg.getIP(); 
			this.ports[msg.getNodeId()] = msg.getPort(); 
		} 
	} 
	
	/**
	 * Broadcasts the array of IP addresses out to each <code>DisSemHelper</code> 
	 * and then sends out the array of ports to each of them. 
	 */ 
	private void broadcastConnectionInfoToDisSemHelpers() {
		// Send out the array of IPs. 
		for (int i = 0; i < this.numberOfNodes; i++) {
			try {
				outs[i].writeObject(this.ips); 
			} catch (IOException e) {
				System.err.println("Error sending IPs to a DisSemHelper."); 
				e.printStackTrace(); 
			} 
		} 
		
		// Send out the array of ports. 
		for (int i = 0; i < this.numberOfNodes; i++) {
			try {
				outs[i].writeObject(this.ports); 
			} catch (IOException e) {
				System.err.println("Error sending ports to a DisSemHelper."); 
				e.printStackTrace(); 
			} 
		}
	} 
	
	/**
	 * Closes the server socket and IO streams. 
	 */ 
	private void closeUpShop() {
		try {
			for (int i = 0; i < this.numberOfNodes; i++) {
				this.ins[i].close(); 
				this.outs[i].flush(); 
				this.outs[i].close(); 
			} 
			this.serverSocket.close(); 
		} catch (IOException e) {
			System.err.println("Error closing socket and/or IO streams."); 
			e.printStackTrace(); 
		} 
	}
	
} 
