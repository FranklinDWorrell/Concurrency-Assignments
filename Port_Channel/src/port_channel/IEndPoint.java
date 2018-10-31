package port_channel; 

import java.io.IOException; 

/**
 * An interface that any <code>EndPoint</code> must implement in order
 * to fulfill a basic contract for such a communication end point. 
 * @author Franklin D. Worrell
 * @version 6 November 2017
 */ 
public interface IEndPoint {
	
	/**
	 * Receive a message sent from a <code>Port</code>. 
	 * @throws IOException
	 * @return the received <code>Message</code> 
	 */ 
	public Message receive() throws IOException; 
	
	/**
	 * Send a message to a listening <code>Port</code> instance.
	 * @param msg the <code>Message</code> to send to the <code>Port</code>
	 */ 
	public void send(Message msg); 
	
	/**
	 * Close this <code>EndPoint</code> so that program can 
	 * terminate gracefully. 
	 * @throws IOException
	 */ 
	public void close() throws IOException;
	
	/**
	 * Configure this <code>EndPoint</code> for connection and connect it
	 * to a <code>Port</code>. 
	 * @throws InterruptedException 
	 */ 
	public void initialize() throws InterruptedException; 
	
} 