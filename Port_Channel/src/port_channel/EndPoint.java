package port_channel; 

import java.io.IOException; 

/**
 * An end point (i.e., a <code>Student</code> for a <code>VirtualClass</code>) 
 * that can send <code>Message</code>s to and receive <code>Message</code>s 
 * from a <code>Port</code>.
 * @author Franklin D. Worrell
 * @version 6 November 2017
 */ 
public abstract class EndPoint implements IEndPoint {
	private int nodeId;
	private String ip; 
	private int portNo; 
	
	public EndPoint(int nodeId, String ip, int portNo) {
		this.nodeId = nodeId;
		this.ip = ip; 
		this.portNo = portNo; 
	} 
	
	/**
	 * Returns the ID of this node in the network (i.e., the 
	 * ID of the <code>Student</code> that has this <code>EndPoint</code>. 
	 * @return this node's ID
	 */ 
	public int getId() {
		return this.nodeId; 
	} 
	
	/**
	 * Returns the IP address used for message passing between the 
	 * <code>Port</code> and the <code>EndPoint</code>. 
	 * @return the IP address where the server <code>Port</code> is. 
	 */ 
	public String getIp() {
		return this.ip; 
	} 
	
	/**
	 * Returns the port number used for message passing.
	 * @return the port number being used for communication
	 */ 
	public int getPortNo() {
		return this.portNo; 
	} 
} 