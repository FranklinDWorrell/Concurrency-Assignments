package semaphore;

import java.io.Serializable;

/**
 * Encapsulates the information needed for messages between the
 * helpers in a distributed semaphore.
 * @author Franklin D. Worrell
 * @version 28 November 2017
 */
public class Message implements Serializable, Comparable<Message> {

	// Constants for message types for semaphore operations. 
	public static final String POP = "POP"; 
	public static final String VOP = "VOP"; 
	public static final String ACK = "ACK"; 

    private int sender;
    private String type;
    private int timestamp;

	/**
	 * Creates a new <code>Message</code> with the specified
	 * content. 
	 * @param sender the id of the node sending this <code>Message</code>
	 * @param type one of the three constant types for this application
	 * @param timestamp the time this message is sent
	 */ 
    public Message(int sender, String type, int timestamp) {
        this.sender = sender;
        this.type = type;
        this.timestamp = timestamp;
    }

    /**
     * Provides a natural ordering for sorting the <code>PriorityQueue</code>
     * used by <code>DisSemHelper</code>.
     * @param otherMessage another <code>Message</code>
     * @return an <code>int</code> reflecting the priority comparison
     */
    @Override
    public int compareTo(Message otherMessage) {
        // Order by timestamps if they are different.
        if (this.timestamp != otherMessage.timestamp) {
            return ((Integer) this.timestamp).compareTo((Integer) otherMessage.timestamp);
        }

        // If timestamps are identical, break tie with sender ID.
        else {
            return ((Integer) this.sender).compareTo((Integer) otherMessage.sender);
        }
    }

	/**
	 * Gets the id of the node in the network that sent this 
	 * <code>Message</code> 
	 * @return the sender node's id
	 */ 
    public int getSender() {
        return sender;
    }

	/**
	 * Returns the type of the <code>Message</code> 
	 * @return the <code>Message</code> type
	 */ 
    public String getType() {
        return type;
    }

	/**
	 * Returns the time this <code>Message</code> was sent. 
	 * @return the timestamp of the <code>Message</code> 
	 */ 
    public int getTimestamp() {
        return timestamp;
    }

}
