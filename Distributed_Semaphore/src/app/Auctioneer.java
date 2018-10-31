package app; 

import connections.Connection; 
import model.Bid; 
import model.BidAcceptance; 
import model.BidRejection; 
import model.BidResults; 
import semaphore.Initiator; 

import java.io.IOException; 
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream; 
import java.net.ServerSocket; 
import java.net.Socket; 
import java.net.SocketException; 

/**
 * The server for a basic electronic application method used to demonstrate 
 * the correctness of the distributed semaphore implementation. 
 * @author Franklin D. Worrell
 * @version 1 December 2017 
 */ 
public class Auctioneer {
	
	private String ip; 
	private int port; 
	private int initiatorPort; 
	private int numNodes; 
	private int auctionLength; 
	private boolean takingBids; 

	private ServerSocket serverSocket; 
	private ObjectInputStream[] ins; 
	private ObjectOutputStream[] outs; 
	
	private double topBid; 
	private int winner; 
	
	public Auctioneer(int port, int initiatorPort, int numNodes, int auctionLength) {
		this.ip = Connection.getLocalNotLoopbackIP(); 
		this.port = port; 
		this.initiatorPort = initiatorPort; 
		this.numNodes = numNodes; 
		this.auctionLength = auctionLength; 
		
		this.takingBids = true; 
		this.outs = new ObjectOutputStream[this.numNodes]; 
		this.ins = new ObjectInputStream[this.numNodes]; 
	} 
	
	/**
	 * Configures the <code>Bidder</code> nodes by waiting for connection 
	 * attempts from them. Starts the <code>Initiator</code> thread then 
	 * sends the <code>Bidder</code> the information for the 
	 * <code>Initiator</code> thread to be used by each node's 
	 * <code>DisSemHelper</code>. 
	 */ 
	private void initialize() {
		this.serverSocket = null; 
		try {
			this.serverSocket = new ServerSocket(this.port); 
			System.out.println("Started Auctioneer on ip " + this.ip + 
							   " and port " + this.port + "."); 
		} catch (IOException e) {
			System.err.println("Error creating server socket. Terminating."); 
			e.printStackTrace(); 
			System.exit(-1); 
		} 
		
		for (int i = 0; i < this.numNodes; i++) {
			try {
				Socket client = this.serverSocket.accept(); 
				this.outs[i] = new ObjectOutputStream(client.getOutputStream()); 
				this.ins[i] = new ObjectInputStream(client.getInputStream()); 
			} catch (IOException e) {
				System.err.println("Error configuring connection to bidder."); 
				e.printStackTrace(); 
			} 
		} 
		
		// Start the Initiator and disseminate its connection information. 
		System.out.println("Starting Initiator and configuration for semaphore."); 
		(new Thread(new Initiator(this.ip, this.initiatorPort, this.numNodes))).start(); 
		this.broadcastInitiatorInfo(); 
		
		// Create and start listeners for each Bidder. 
		System.out.println("Creating listeners for Bidders."); 
		for (int i = 0; i < this.numNodes; i++) {
			(new Thread(new AuctioneerListener(this, this.ins[i], this.outs[i]))).start(); 
		} 
	} 
	
	/**
	 * Informs all of the <code>Bidder</code> nodes in the network 
	 * of the connection information for the <code>Initiator</code> 
	 * used to coordinate the distributed semaphore. Each participating 
	 * <code>Bidder</code>s will pass the information along until it is 
	 * ultimately received by a <code>DisSemHelper</code>. 
	 */ 
	private void broadcastInitiatorInfo() {
		for (int i = 0; i < this.numNodes; i++) {
			try {
				this.outs[i].writeObject(this.ip); 
			} catch (IOException e) {
				System.err.println("Error sending initiator IP to nodes."); 
				e.printStackTrace(); 
			} 
		} 
		
		for (int i = 0; i < this.numNodes; i++) {
			try {
				this.outs[i].writeObject(this.initiatorPort); 
			} catch (IOException e) {
				System.err.println("Error sending initiator port to nodes."); 
				e.printStackTrace(); 
			}
		}
	} 
	
	/**
	 * Sends the outcome of the auction to each of the participating 
	 * <code>Bidder</code>s. 
	 */ 
	private void broadcastResults() {
		System.out.println("Broadcasting results to Bidders."); 
		for (int i = 0; i < this.numNodes; i++) {
			try {
				this.outs[i].writeObject(new BidResults(this.winner, this.topBid));
			} catch (IOException e) {
				System.err.println("Error writing results to bidders."); 
				e.printStackTrace(); 
			}
		} 
	}
	
	/**
	 * Closes the <code>ServerSocket</code> and all the IO streams 
	 * previously used in communication. 
	 */ 
	private void closeUpShop() {
		System.out.println("Closing connections to Bidders."); 
		try {
			for (int i = 0; i < this.numNodes; i++) {
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
	
	public boolean getTakingBids() {
		return this.takingBids; 
	} 
	
	public void setTakingBids(boolean takingBids) {
		this.takingBids = takingBids; 
	} 
	
	public double getTopBid() {
		return this.topBid; 
	} 
	
	public void setTopBid(double bid) {
		this.topBid = bid; 
	} 
	
	public int getWinner() {
		return this.winner; 
	} 
	
	public void setWinner(int newWinner) {
		this.winner = newWinner; 
	} 
	
	/**
	 * Sets up and coordinates an auction--the <code>Auctioneer</code> 
	 * acts more or less like an auction hall, with the heavy lifting 
	 * being done by the <code>AuctionListener</code> associated 
	 * uniquely with a <code>Bidder</code> who is participating. 
	 */ 
	public static void main(String[] args) {
		// Validate command-line input. 
		if (args.length != 4) {
			System.err.println("Usage: java Auctioneer port " + 
							   "initiator-port num-bidders length-of-auction"); 
			System.exit(-1); 
		} 
		
		// Parse the command-line input. 
		int port = Integer.parseInt(args[0]); 
		int initiatorPort = Integer.parseInt(args[1]); 
		int numNodes = Integer.parseInt(args[2]); 
		int auctionLength = Integer.parseInt(args[3]); 
		
		// Get things set up and running. 
		Auctioneer auctioneer = new Auctioneer(port, initiatorPort, 
				numNodes, auctionLength); 
		auctioneer.initialize(); 
		
		// Allow listener to conduct auction business. 
		System.out.println("Auction is in progress."); 
		try {
			Thread.sleep(auctionLength * 1000); 
		} catch (InterruptedException e) {
			System.err.println("Interrupted while waiting for results."); 
			e.printStackTrace(); 
		} 
		
		System.out.println("Auction complete! Results will be broadcast."); 
		System.out.println(auctioneer.getWinner() + " won with a bid of $" + 
						   auctioneer.getTopBid() + ". "); 
		
		// End auction, collect results, and notify bidders of outcome. 
		auctioneer.setTakingBids(false); 
		// Send results to all Bidders. 
		auctioneer.broadcastResults(); 
		// Clean it all up. 
		auctioneer.closeUpShop(); 
		
		// Terminate and clean up. 
		System.exit(0); 
	}
	
	/**
	 * A thread that listens for a <code>Bid</code> from a 
	 * <code>Bidder</code> participating in the auction. 
	 */ 
	private class AuctioneerListener implements Runnable {
		 
		private Auctioneer auctioneer; 
		private ObjectInputStream in; 
		private ObjectOutputStream out; 
		
		public AuctioneerListener(Auctioneer auctioneer, 
					ObjectInputStream in, ObjectOutputStream out) {
			this.auctioneer = auctioneer; 
			this.in = in; 
			this.out = out; 
		} 
		
		/**
		 * Accepts or rejects a bid on behalf of the <code>Auctioneer</code>
		 * and notifies the <code>Bidder</code> of the <code>Bid</code>'s 
		 * status. This method would cause race conditions to emerge if the 
		 * ability to place a bid was not guarded by the distributed semaphore. 
		 * @param bid an incoming bid for the <code>Auctioneer</code> to consider 
		 */ 
		private void handleBid(Bid bid) {
			System.out.println("Received bid of $" + bid.getBidValue() + " from " + 
							   bid.getSender() + "."); 
			// This bid is currently the highest. 
			if ((bid.getBidValue() - this.auctioneer.getTopBid()) > 0.009) {
				this.auctioneer.setWinner(bid.getSender()); 
				this.auctioneer.setTopBid(bid.getBidValue()); 
				try {
					out.writeObject(new BidAcceptance(bid.getBidValue())); 
					System.out.println("Bid accepted. Acknowledging."); 
				} catch (IOException e) {
					System.err.println("Error accepting bid from " + bid.getSender()); 
					e.printStackTrace(); 
				} 
			} 
			
			// Bid is below the current highest. 
			else {
				try {
					out.writeObject(new BidRejection(bid.getBidValue(), 
												this.auctioneer.getTopBid())); 
					System.out.println("Bid rejected. Acknowledging."); 
				} catch (IOException e) {
					System.err.println("Error rejecting bid from " + bid.getSender()); 
					e.printStackTrace(); 
				} 
			} 				
		}
		
		/** 
		 * Waits to receive a <code>Bid</code> from the <code>Bidder</code> 
		 * it this listener is paired with. 
		 */ 
		@Override
		public void run() {
			while (this.auctioneer.getTakingBids()) {
				Bid bid = null; 
				try {
					bid = (Bid) this.in.readObject();
				} catch (IOException | 
						 ClassNotFoundException e) {
					// If bidding was over, no worries. 
					if (!this.auctioneer.getTakingBids()) {
						return; 
					} 
					else {
						System.err.println("Error taking bids from bidder."); 
						e.printStackTrace(); 
						System.exit(-1); 
					}
				}
				
				if (bid != null) {
					this.handleBid(bid); 
				} 
			} 
		} 
	} 
	
} 
