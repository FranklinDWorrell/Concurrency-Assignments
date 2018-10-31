package model; 

/**
 * A message used by the <code>Auctioneer</code> to inform a <code>Bidder</code> 
 * that their bid was rejected because it was the lower than the current 
 * winning bid. 
 * @author Franklin D. Worrell
 * @version 29 November 2017 
 */ 
public class BidRejection implements BidResponse {

	private double bid; 
	private double currentTop; 
	
	public BidRejection(double bid, double currentTop) {
		this.bid = bid; 
		this.currentTop = currentTop; 
	} 
	
	@Override
	public String getMessage() {
		return "Your bid of $" + this.bid + " was rejected. Current minimum " + 
			"bid is $" + (this.currentTop + 0.01) + "."; 
	} 
	
} 
