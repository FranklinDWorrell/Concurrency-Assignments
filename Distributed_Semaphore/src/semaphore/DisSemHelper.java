package semaphore; 

import connections.ChannelEndPoint; 
import connections.ChannelPort;
import utilities.LamportLogicalClock;

import java.io.IOException; 
import java.io.ObjectInputStream; 
import java.io.ObjectOutputStream; 
import java.net.Socket; 
import java.net.UnknownHostException; 
import java.util.PriorityQueue;
import java.util.Arrays; 

/**
 * 
 * @author Franklin D. Worrell
 * @version 28 November 2017
 */ 
public class DisSemHelper {
	
	private int id; 
	private String ip; 
	private int port; 
	private String initiatorIP; 
	private int initiatorPort; 
	private String[] nodeIPs; 
	private int[] nodePorts; 

	private DistributedSemaphore semaphore; 
	private int s;
	private boolean hasSemaphore; 
	private LamportLogicalClock clock;
	private int[] watermarks;
    private PriorityQueue<Message> pQueue;
	private PriorityQueue<Message> vQueue;
	private ChannelPort channelPort;

    public DisSemHelper(DistributedSemaphore semaphore, int id, String ip, int port, 
						String initiatorIP, int initiatorPort) {
		this.id = id; 
		this.ip = ip; 
		this.port = port; 
		this.initiatorIP = initiatorIP; 
		this.initiatorPort = initiatorPort; 
		this.hasSemaphore = false; 

		this.semaphore = semaphore; 
		this.s = 1;
		this.clock = new LamportLogicalClock();
        this.pQueue = new PriorityQueue<>();
		this.vQueue = new PriorityQueue<>();
	}
	
	public void acquireSemaphore() {
		this.channelPort.broadcast(new Message(this.id, Message.POP, clock.getTime())); 
		this.clock.tick(); 
	}
	
	public void releaseSemaphore() {
		this.hasSemaphore = false; 
		this.channelPort.broadcast(new Message(this.id, Message.VOP, clock.getTime())); 
		this.clock.tick(); 
	} 
	
	public synchronized boolean hasSemaphore() {
		return this.hasSemaphore; 
	} 
	
	public void handleMessage(Message msg) {
		// Irrespective of message type, handle logical clock. 
		this.clock.compareSetAndTick(msg.getTimestamp()); 
		
		if (Message.POP.equals(msg.getType())) {
			System.out.println("DSH handling POP."); 
			this.pQueue.offer(msg); 
			this.channelPort.broadcast(new Message(this.id, Message.ACK, clock.getTime())); 
			this.clock.tick(); 
		}
		
		else if (Message.VOP.equals(msg.getType())) {
			System.out.println("DSH handling VOP."); 
			this.vQueue.offer(msg); 
			this.channelPort.broadcast(new Message(this.id, Message.ACK, clock.getTime())); 
			this.clock.tick(); 
		}
		
		else { // Message was an acknowledgement--here's where it get tricky. 
			System.out.println("DSH handling ACK."); 
			this.watermarks[msg.getSender()] = msg.getTimestamp(); 
			int fullyAcknowledged = Arrays.stream(this.watermarks).min().getAsInt(); 
			
			// Pop all fully acknowledged VOP messages and increment s accordingly. 
			for (int i = 0; i < this.vQueue.size(); i++) {
				Message next = this.vQueue.peek(); 
				if (fullyAcknowledged <= next.getTimestamp()) {
					break; 
				} else {
					this.vQueue.poll(); 
					this.s++; 
				} 
			}
			
			// Pop all fully acknowledged POP messages while s > 0. 
			for (int i = 0; i < this.pQueue.size() && this.s > 0; i++) {
				Message next = this.pQueue.peek(); 
				if (fullyAcknowledged <= next.getTimestamp()) {
					break; 
				} else {
					this.s--; 
					Message popped = this.pQueue.poll();
					// The request from this helper has been reached in queue. 
					if (popped.getSender() == this.id) {
						System.out.println("DSH flagging semaphore acquired."); 
						this.hasSemaphore = true; 
					}
				}
			} 
		}	
	} 
	
	private void findInitiatorAndGetNetworkInfo() { 
		Socket socket = null; 
		ObjectOutputStream outToInit = null; 
		ObjectInputStream inFromInit = null; 
		
		try {
			socket = new Socket(this.initiatorIP, this.initiatorPort); 
			outToInit = new ObjectOutputStream(socket.getOutputStream()); 
			inFromInit = new ObjectInputStream(socket.getInputStream()); 
		} catch (UnknownHostException e) {
			System.err.println("Error establishing connection to Initiator socket. Terminating"); 
			e.printStackTrace(); 
			System.exit(-1); 			
		} catch (IOException e) {
			System.err.println("Error establishing communication with Initiator. Terminating"); 
			e.printStackTrace(); 
			System.exit(-1); 
		}
		
		// Send information about this node to Initiator. 
		this.sendInfoToInitiator(outToInit); 
		
		// Get the arrays of information about the other nodes. 
		this.getInfoAboutOtherNodesFromInitiator(inFromInit); 
		
		// Connection to Initiator no longer needed--clean up everything. 
		try {
			socket.close(); 
			outToInit.close(); 
			inFromInit.close(); 
		} catch (IOException e) {
			System.err.println("Error closing connections to Initiator."); 
			e.printStackTrace(); 
		} 
	} 
	
	/**
	 * Uses the output stream passed as an argument to send connection info 
	 * to the <code>Initiator</code> thread coordinating communication between 
	 * the <code>DisSemHelper</code>s. 
	 * @param out the <code>ObjectOutputStream</code> writing to the <code>Initiator</code> 
	 */ 
	private void sendInfoToInitiator(ObjectOutputStream out) {
		ConnectionInfoMessage info = new ConnectionInfoMessage(this.id, this.ip, this.port); 
		try {
			out.writeObject(info);
		} catch (IOException e) {
			System.err.println("Error sending node connection info to Initiator."); 
			e.printStackTrace(); 
		} 
	} 
	
	/**
	 * Collects the connection information for the other nodes in the 
	 * distributed semaphore network. Saves the info to the appropriate 
	 * instance variables. 
	 * @param in the <code>ObjectInputStream</code> connected to <code>Initiator</code> 
	 */ 
	private void getInfoAboutOtherNodesFromInitiator(ObjectInputStream in) {
		try { 
			this.nodeIPs = (String[]) in.readObject(); 
		} catch (ClassNotFoundException | IOException e) {
			System.err.println("Error getting array of IPs from the Initiator."); 
			e.printStackTrace(); 
		} 
		try {
			this.nodePorts = (int[]) in.readObject(); 
		} catch (ClassNotFoundException | IOException e) {
			System.err.println("Error getting array of ports from the Initiator."); 
			e.printStackTrace(); 
		} 
	} 
	
	/**
	 * 
	 */ 
	public void init() {
		// Find the Initiator and wire everything together. Call blocks. 
		this.findInitiatorAndGetNetworkInfo(); 
		this.watermarks = new int[this.nodeIPs.length]; 
		this.channelPort = new ChannelPort(this.port, this.nodeIPs.length); 
		
		// Start the myriad Threads helping out and configure the port's IO streams. 
		(new Thread(this.channelPort)).start(); 
		(new EndPointSpinner(this.channelPort, this.nodeIPs, this.nodePorts)).startAllEndPoints(); 
		(new Thread(new HelperListener(this, this.channelPort))).start(); 
	}
	
	/**
	 * Creates all the IO streams needed by the <code>ChannelPort</code> 
	 * and passes necessary references to streams to the port. 
	 */ 
	private class EndPointSpinner {
		
		private ChannelPort port; 
		private String[] nodeIPs; 
		private int[] nodePorts; 
		
		public EndPointSpinner(ChannelPort port, String[] nodeIPs, int[] nodePorts) {
			this.port = port; 
			this.nodeIPs = nodeIPs; 
			this.nodePorts = nodePorts; 
		} 
		
		public void startAllEndPoints() {
			for (int i = 0; i < this.nodeIPs.length; i++) {
				int cepID = i; 
				String cepIP = nodeIPs[i]; 
				int cepPort = nodePorts[i]; 
				(new Thread(new Runnable(){ 
					@Override
					public void run() {
						ChannelEndPoint cep = new ChannelEndPoint(cepID, cepIP, cepPort); 
						cep.initialize(); 
						while (true) {
							try {
								Message msg = cep.receive(); 
								port.gotMessage(msg); 
							} catch (IOException e) {
								System.err.println("Error getting broadcasted messages."); 
								e.printStackTrace(); 
							} 
						} 
					}
				})).start(); 
			} 
		} 
	} 
	
} 