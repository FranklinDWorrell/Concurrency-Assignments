package port_channel; 

/**
 * A <code>MessageType</code> that models a <code>Teacher</code>
 * dismissing a class.
 * @author Franklin D. Worrell
 * @version 30 October 2017
 */ 
public class Dismissal extends Message {
	
	public Dismissal(int timestamp) {
		super(-1, timestamp, "Class dismissed"); 
	} 
}