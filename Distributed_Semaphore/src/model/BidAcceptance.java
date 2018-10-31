package model; 

/**
 * A message that acknowledges that a placed bid is currently the 
 * highest bid yet placed. 
 * @author Franklin D. Worrell
 * @version 29 November 2017 
 */ 
public class BidAcceptance implements BidResponse {
	
	private double bid; 
	
	public BidAcceptance(double bid) {
		this.bid = bid; 
	}
	
	@Override
	public String getMessage() {
		return "Your bid of $" + this.bid + " was accepted!"; 
	} 
	
}