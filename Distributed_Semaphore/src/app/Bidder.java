package app; 

import connections.Connection; 
import model.Bid; 
import model.BidResponse; 
import model.BidResults; 
import semaphore.DistributedSemaphore; 

import java.io.IOException; 
import java.io.ObjectInputStream; 
import java.io.ObjectOutputStream; 
import java.net.Socket; 
import java.net.UnknownHostException; 

/**
 * A participant in a distributed electronic auction application. 
 * Uses a distributed semaphore to gain access to the critical 
 * section--i.e., place a bid on the item up for auction. Includes 
 * option for passing an additional command-line argument to signal 
 * a test requested by the Professor: call semaphore.v() then 
 * semaphore.p() in a loop. 
 * @author Franklin D. Worrell
 * @version 7 December 2017
 */ 
public class Bidder { 

	private int id; 
	private String ip; 
	private int semPort; 
	private String auctioneerIP; 
	private int auctioneerPort; 
	
	private ObjectInputStream in; 
	private ObjectOutputStream out; 
	private Socket socket; 
	
	private DistributedSemaphore semaphore; 
	
	public Bidder(int id, int port, String auctioneerIP, int auctioneerPort) {
		this.id = id; 
		this.ip = Connection.getLocalNotLoopbackIP(); 
		this.semPort = port; 
		this.auctioneerIP = auctioneerIP; 
		this.auctioneerPort = auctioneerPort; 
		this.semaphore = null; 
	} 
	
	/**
	 * Establishes a connection with the <code>Auctioneer</code> server 
	 * and creates the IO streams needed for communication with it. 
	 */ 
	private void configure() {
		// Establish connection with auctioneer. 
		this.socket = null; 
		try {
			this.socket = new Socket(this.auctioneerIP, this.auctioneerPort); 
			this.in = new ObjectInputStream(this.socket.getInputStream()); 
			this.out = new ObjectOutputStream(this.socket.getOutputStream()); 
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host."); 
			e.printStackTrace(); 
			System.exit(-1); 
		} catch (IOException e) {
			System.err.println("Error acquiring IO for communication with Auctioneer."); 
			e.printStackTrace(); 
			System.exit(-1); 
		} 
		
		// Get connection information for Initiator, create distributed semaphore. 
		this.getInitiatorInfoAndCreateSemaphore(); 
	} 
	
	/**
	 * Retrieves the information for the <code>Initiator</code> that will 
	 * be used to configure the connections between the <code>DisSemHelper</code>s 
	 * and then creates a <code>DistributedSemaphore</code> and passes the 
	 * <code>Initiator</code>'s connection information to it. 
	 */ 
	private void getInitiatorInfoAndCreateSemaphore() {
		try {
			String initiatorIP = (String) this.in.readObject(); 
			int initiatorPort = (Integer) this.in.readObject(); 
			System.out.println("Received initiator ip " + initiatorIP + 
							   " and port " + initiatorPort + "."); 
			this.semaphore = new DistributedSemaphore(this.id, this.ip, this.semPort, 
					initiatorIP, initiatorPort); 
			this.semaphore.startHelper(); 
		} catch (IOException | ClassNotFoundException e) {
			System.err.println("Error receiving initiator info from Auctioneer."); 
			e.printStackTrace(); 
			System.exit(-1); 
		} 		
	} 
	
	/**
	 * Acquires the semaphore required to place a bid with the 
	 * <code>Auctioneer</code>, places a bid, and then releases 
	 * the semaphore. 
	 * @param bid the <code>Bidder</code>'s <code>Bid</code> 
	 */ 
	private BidResponse bid(Bid bid) {
		// Acquire the semaphore before placing bid. 
		this.semaphore.p(); 
		
		// Place bid. 
		try {
			System.out.println("Acquired semaphore. Placing bid."); 
			this.out.writeObject(bid); 
			Thread.sleep(100); 
		} catch (IOException e) {
			System.err.println("Error sending bid to Auctioneer."); 
			e.printStackTrace(); 
		} catch (InterruptedException e) {
			System.err.println("Error sleeping while holding semaphore--bad news!"); 
			e.printStackTrace(); 
		} 
		
		// Get feedback from Auctioneer on bid status. 
		BidResponse response = null; 
		try {
			response = (BidResponse) this.in.readObject(); 
		} catch (ClassNotFoundException | IOException e) {
			System.err.println("Error receiving bid response from Auctioneer."); 
			e.printStackTrace(); 
		} 
		
		System.out.println("Finished placing bid. Releasing semaphore."); 
		
		// Release the semaphore. 
		this.semaphore.v(); 

		return response; 
	} 
	
	/**
	 * Awaits results to be received from the <code>Auctioneer</code> 
	 * and then returns them as a <code>String</code> once they are 
	 * received. 
	 * @return the message sent by the <code>Auctioneer</code> 
	 */ 
	private String getAuctionResults() {
		BidResults results = null; 
		try {
			results = (BidResults) in.readObject(); 
		} catch (ClassNotFoundException | IOException e) {
			System.err.println("Error receiving auction results from Auctioneer."); 
		} 
		
		return results == null ? null : results.getMessage(); 
	} 
	
	/**
	 * Closes the IO streams associated with this <code>Bidder</code>. 
	 */ 
	private void closeUpShop() {
		try {
			this.in.close(); 
			this.out.close(); 
			this.socket.close(); 
		} catch (IOException e) {
			System.err.println("Error closing IO streams."); 
			e.printStackTrace(); 
		}
	} 
	
	public static void main(String[] args) {
		// Validate command-line arguments. 
		if (!(args.length == 6 || args.length == 7)) {
			System.err.println("Usage: java Bidder id open-port" + 
							   "auctioneer-ip auctioneer-port wait bid [vptest]"); 
			System.exit(-1); 
		} 
		
		// Parse command-line arguments
		int id = Integer.parseInt(args[0]); 
		int openPort = Integer.parseInt(args[1]); 
		String auctioneerIP = args[2]; 
		int auctioneerPort = Integer.parseInt(args[3]); 
		int wait = Integer.parseInt(args[4]); 
		double bid = Double.parseDouble(args[5]); 
		
		// Start and configure Bidder instance. 
		Bidder bidder = new Bidder(id, openPort, auctioneerIP, auctioneerPort); 
		bidder.configure(); 

        // Special test case that Professor Tu explicitly requested. 
		if (args.length == 7) { 
			System.out.println("After given wait, Bidder will attempt to illicitly " + 
							   "acquire semaphore and bid once a second for 5 " + 
							   "seconds with each bid $0.01 more than the last."); 
			try {
				Thread.sleep(wait * 1000); 
			} catch (InterruptedException e) {
				System.err.println("Error pausing before placing bid."); 
				e.printStackTrace(); 
			} 
		
			for (int i = 0; i < 150; i++) {		
				System.out.println("Placing bid of $" + bid + " dollars."); 
				bidder.semaphore.v(); 
				BidResponse response = bidder.bid(new Bid(id, bid));
				System.out.println(response.getMessage()); 
				bid += 0.01; 
			}
		} 

		// Otherwise, act civilized. 
		else {
			System.out.println("Bidder number " + id + " here. Will bid in " + 
							   wait + " seconds."); 
		
			try {
				Thread.sleep(wait * 1000); 
			} catch (InterruptedException e) {
				System.err.println("Error pausing before placing bid."); 
				e.printStackTrace(); 
			} 
		
			System.out.println("Placing bid of $" + bid + " dollars."); 
			BidResponse response = bidder.bid(new Bid(id, bid));
			System.out.println(response.getMessage()); 
		} 

		System.out.println(bidder.getAuctionResults()); 
		bidder.closeUpShop(); 
		
		// Terminate and clean up. 
		System.exit(0); 
	} 
	
} 
