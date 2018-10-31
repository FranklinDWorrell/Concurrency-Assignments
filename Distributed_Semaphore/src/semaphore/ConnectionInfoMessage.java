package semaphore; 

import java.io.Serializable; 

/**
 * Used for communicating connection information from a <code>DisSemHelper</code> 
 * to the <code>Initiator</code> thread that will broadcast this info back out 
 * to all the connected <code>DisSemHelper</code>s, albeit in a different 
 * format. 
 * @author Franklin D. Worrell
 * @version 29 November 2017 
 */ 
public class ConnectionInfoMessage implements Serializable {
	
	private int nodeId; 
	private String ip; 
	private int port; 
	
	public ConnectionInfoMessage(int nodeId, String ip, int port) {
		this.nodeId = nodeId; 
		this.ip = ip; 
		this.port = port; 
	} 
	
	/**
	 * Returns the unique identifier of the sender node in the network. 
	 * @return the sender's ID in the network 
	 */ 
	public int getNodeId() {
		return this.nodeId; 
	} 
	
	/**
	 * Returns the IP address of the sender node. 
	 * @return the sender's IP address 
	 */ 
	public String getIP() {
		return this.ip; 
	} 
	
	/**
	 * Returns the port number of the sender node on its IP. 
	 * @return the sender's port used for communication 
	 */ 
	public int getPort() {
		return this.port; 
	} 
	
}
