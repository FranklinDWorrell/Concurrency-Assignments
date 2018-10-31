package port_channel;

/**
 * Models a student raising her/his hand to ask the
 * <code>Teacher</code> a question.
 * @author Franklin D. Worrell
 * @version 30 October 2017
 */ 
public class HandRaise extends Message {
	
	/**
	 * Initializes a <code>HandRaise</code> message, which 
	 * is just an instance of <code>MessageType</code> with
	 * a preset <code>msgType</code>. 
	 * @param studentId the student whose hand is being raised
	 * @param timestamp when the student's hand was raised
	 */ 
	public HandRaise(int studentId, int timestamp) {
		super(studentId, timestamp, ""); 
	} 
}