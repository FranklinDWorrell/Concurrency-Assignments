package port_channel;

import java.io.Serializable;

/**
 * Models a general message sent in a <code>VirtualClass</code>
 * environment. The pId of -1 is reserved for the <code>Teacher</code>. 
 *
 * Modified from code provided by the instructor. 
 */ 
public class Message implements Serializable {
	int pId;
	int timestamp; 
	String text; 

	public Message(int pId, int timestamp, String text) {
		this.pId = pId;
		this.timestamp = timestamp; 
		this.text = text; 
	}

	/**
	 * Returns the <code>studentId</code> of the <code>Student</code> 
	 * who wishes to ask a question. 
	 * @return the ID of the student who has a question
	 */ 
	public int getStudentId() {
		return this.pId; 
	} 
	
	/**
	 * Returns the time of this message was created in terms 
	 * of the progress of the lecture it was sent during. 
	 * @return the message's timestamp
	 */ 
	public int getTimestamp() {
		return this.timestamp; 
	} 

	/**
	 * Returns the text contained in this message. 
	 * @return the <code>Message</code>'s text
	 */ 
	public String getText() {
		return this.text; 
	}
}
