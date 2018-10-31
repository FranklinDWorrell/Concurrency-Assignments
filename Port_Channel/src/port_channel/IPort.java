package port_channel; 

/**
 * An interface defining the methods that any <code>Port</code>
 * must implement to fulfill the contract.
 * @author Franklin D. Worrell
 * @version 6 November 2017
 */ 
public interface IPort {

	/**
	 * Sends a <code>Message</code> to each <code>EndPoint</code>
	 * that is listening.
	 * @param msg the <code>Message</code> to send out
	 */ 
	public void broadcast(Message msg); 
	
	/**
	 * Closes all connections to every listening <code>EndPoint</code>.
	 */ 
	public void closeAllOutputWriters(); 
	
} 