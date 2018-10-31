package model;

/**
 * A message used to communicate the winner of an auction to all 
 * the <code>Bidder</code>s that are participating. 
 * @author Franklin D. Worrell
 * @version 28 November 2017
 */ 
public class BidResults implements BidResponse {
	
	private int winner; 
	private double bid; 
	
	public BidResults(int winner, double bid) {
		this.winner = winner; 
		this.bid = bid; 
	} 
	
	/**
	 * Returns the message communicating the auction winner. 
	 * @return a <code>String</code> containing the winner and her bid 
	 */ 
	public String getMessage() {
		return "Bidder " + winner + " won with a bid of $" + bid + "."; 
	}
	
} 
