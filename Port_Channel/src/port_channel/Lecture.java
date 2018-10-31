package port_channel; 

/**
 * This class models a snippet of the lecture being
 * delivered by the <code>Teacher</code>. 
 * @author Franklin D. Worrell
 * @version 30 October 2017
 */ 
public class Lecture extends Message {
	
	public Lecture(int timestamp, String text) {
		super(-1, timestamp, text); 
	} 
} 