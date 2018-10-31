package model; 

import java.io.Serializable; 

/**
 * A basic interface for polymorphic processing of messages 
 * sent from <code>Auctioneer</code> to <code>Bidder</code>. 
 * @author Franklin D. Worrell
 * @version 28 November 2017 
 */ 
public interface BidResponse extends Serializable {
	
	/**
	 * Returns the content of this message in <code>String</code> 
	 * format. 
	 * @return the contents of this response to a <code>Bid</code>
	 */ 
	String getMessage(); 
	
} 