package connections;

import semaphore.Message;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A port (i.e., a Teacher in the <code>VirtualClass</code>) 
 * that has the ability to send and broadcast a <code>Message</code> 
 * to and receive a <code>Message</code> from an <code>EndPoint</code>. 
 * The <code>connections.Port</code> thread effectively manages a message queue that
 * is used by the <code>Teacher<code> for receiving questions from 
 * <code>Student</code>s and facilitates broadcasting messages to all of
 * the <code>Student</code>s.  
 * @author Franklin D. Worrell
 * @version 28 November 2017
 */ 
public abstract class Port implements Runnable {
	int portNo; 
	int networkSize; 
	ConcurrentLinkedQueue<Message> queue;
	boolean doneBroadcasting; 
	
	public Port(int portNo, int networkSize) {
		this.portNo = portNo; 
		this.networkSize = networkSize; 
		this.queue = new ConcurrentLinkedQueue<>(); 
		this.doneBroadcasting = false; 
	}
	
	/**
	 * Sends a <code>Message</code> to each <code>EndPoint</code>
	 * that is listening.
	 * @param msg the <code>Message</code> to send out
	 */ 
	public abstract void broadcast(Message msg);
	
	/**
	 * Closes all connections to every listening <code>EndPoint</code>.
	 */ 
	public abstract void closeAllOutputWriters(); 
	
	/**
	 * Sets the flag used to indicate the end of communication between 
	 * the <code>connections.Port</code> and each <code>EndPoint</code>.
	 * @param status new value for <code>this.doneBroadcasting</code>
	 */ 
	public void setDoneBroadcasting(boolean status) {
		this.doneBroadcasting = status;
	}
	
	/**
	 * Returns whether or not this <code>connections.Port</code> intends to send
	 * further messages to any listening <code>EndPoints</code>.
	 * @return whether there will be subsequent calls to <code>this.broacast</code>
	 */ 
	public boolean isDoneBroadcasting() {
		return this.doneBroadcasting; 
	} 
	
	/**
	 * Returns the port number being used for communication between
	 * the <code>connections.Port</code> and each connected <code>EndPoint</code>.
	 * @return the port number used by the network
	 */ 
	public int getPortNo() {
		return this.portNo; 
	}
	
	/**
	 * Returns the number of nodes in the network besides the 
	 * <code>connections.Port</code> itself.
	 * @return the number of connected <code>EndPoint</code>s
	 */ 
	public int getNetworkSize() {
		return this.networkSize; 
	} 
	
	/**
	 * Returns a reference to the <code>connections.Port</code>'s message
	 * queue.
	 * @return the <code>ConcurrentLinkedQueue</code> used by this
	 */ 
	public ConcurrentLinkedQueue<Message> getQueue() {
		return this.queue;
	} 
	
	/**
	 * Enqueues a new <code>Message</code> and then notifies
	 * any objects waiting for the queue to contain something.
	 * @param message the <code>Message</code> to enqueue
	 */ 
	public synchronized void gotMessage(Message message) {
		this.getQueue().offer(message); 
		notifyAll(); 		
	} 	
	
	/**
	 * Block until a <code>Message</code> is placed in the
	 * message queue. 
	 * @return the <code>Message</code> pulled from the queue
	 */ 
	public synchronized Message receive() { 
		while (this.queue.isEmpty()) { 
			try {
				wait(); 
			} catch (InterruptedException ire) {
				ire.printStackTrace();
			}
		} 
		return this.queue.poll(); 
	} 

	/**
	 * Returns whether or not there is a <code>Message</code> waiting 
	 * in the queue for processing.
	 * @return this.que.isEmpty()
	 */ 
	public boolean hasMessageWaiting() {
		return !this.getQueue().isEmpty(); 
	}
	
	/**
	 * Puts a previously removed <code>Message</code> back into the queue. 
	 * @param msg the <code>Message</code> to reinsert into the queue
	 */ 
	public void requeueMessage(Message msg) {
		this.getQueue().offer(msg); 
	}	

	/**
	 * Finds and returns a <code>Message</code> from the specified 
	 * sender that is of the specified type. Returns null is no such
	 * <code>Message</code> is found in the queue. Preserves the 
	 * order of the queue. 
	 * @param studentId the sender of the desired <code>Message</code>
	 * @param targetType the type of the sought after <code>Message<code>
	 * @return the first <code>Message</code> in queue matching parameters or null if no match found
	 */
	public Message findMessage(int studentId, Class targetType) {
		// Find the first message of the specified type and sender. 
		while (true) {
			Message received = this.queue.poll();
			
			// Find this particular student's question. 
			if (targetType.isInstance(received)) {
				return received; 
			} 
			
			// Message was not the desired question, requeue the message. 
			if (received != null) {
				this.queue.offer(received); 
			} 
		} 
	}
}