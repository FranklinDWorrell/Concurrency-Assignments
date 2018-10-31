package model; 

import java.io.Serializable; 

/**
 * A bid message to be passed from a <code>Bidder</code> 
 * to the <code>Auctioneer</code>. 
 * @author Franklin D. Worrell
 * @version 30 November 2017 
 */ 
public class Bid implements Serializable {
	
	private int sender; 
	private double bid; 
	
	public Bid(int sender, double bid) {
		this.sender = sender; 
		this.bid = bid; 
	} 
	
	/**
	 * Returns the amount of the <code>Bidder</code>'s bid. 
	 * @return the bid amount
	 */ 
	public double getBidValue() {
		return this.bid; 
	} 
	
	/**
	 * Returns the id of the <code>Bidder</code> that offered 
	 * this <code>Bid</code>. 
	 * @return the id of the <code>Bidder</code> tha submitted bid
	 */ 
	public int getSender() {
		return this.sender; 
	} 
	
} 
