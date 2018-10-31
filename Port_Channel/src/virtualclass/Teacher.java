package virtualclass; 

import port_channel.Dismissal; 
import port_channel.HandRaise; 
import port_channel.Lecture; 
import port_channel.Message; 
import port_channel.Port; 
import port_channel.Question; 
import java.util.concurrent.ConcurrentLinkedQueue; 

/**
 * This class models a teacher in a classroom setting for the 
 * <code>VirtualClass</code> program. The <code>Teacher</code> 
 * lectures, acknowledges a <code>Student</code> when she or he
 * asks a <code>Question</code>, and then answers the question. 
 * This <code>Thread</code> simply lectures at a steady pace and 
 * answers <code>Student</code> questions as they arrive. 
 * @author Franklin D. Worrell
 * @version 30 October 2017
 */ 
public class Teacher implements Runnable {
	private Port port; 
	private int lengthOfLecture; 
	private int lectSentCount; 
	
	/**
	 * Intializes a new Teacher instance with the <code>Port</code>
	 * instance provided by <code>VirtualClass</code>. 
	 * @param port the <code>Port</code> that will send/receive/broadcast 
	 * @param lengthOfLecture number of broadcasts comprising the lecture 
	 */ 
	public Teacher(Port port, int lengthOfLecture) {
		this.port = port; 
		this.lengthOfLecture = lengthOfLecture; 
		this.lectSentCount = 0; 
	} 
	
	/**
	 * The logic of calling on a <code>Student</code> after she
	 * has risen her hand. After the <code>Student</code> is 
	 * acknowledged, a student's <code>Question</code> is produced. 
	 * @param msg the <code>HandRaise</code> sent by a <code>Student</code>
	 * @return the <code>Question</code> of the inquiring <code>Student</code>
	 */ 
	private Question callOnStudent(HandRaise msg) {
		int sId = msg.getStudentId(); 
		this.port.broadcast(new Message(-1, this.lectSentCount, 
						  "Yes, Student " + sId + ", what is your question?")); 
		
		Class questionType = null; 
		
		try {
			questionType = Class.forName("port_channel.Question"); 
		} catch (ClassNotFoundException e) {
			System.err.println("Error using reflection.");
			e.printStackTrace(); 
		} 
		return (Question) this.port.findMessage(sId, questionType); 
	} 
	
	/**
	 * Gives the floor to the <code>Student</code> to ask a 
	 * <code>Question</code> in front of the entire class and
	 * then answers the student's question cryptically. 
	 * @param question the <code>Question</code> the <code>Student</code> asked 
	 */ 
	private void askAndAnswerQuestion(Question question) {
		this.port.broadcast(question); 
		this.port.broadcast(new Message(-1, this.lectSentCount,
						  "Student " + question.getStudentId() + "," + 
						  "that is an excellent question. " + 
						  "The answer will become apparent " + 
						  "in the remaining lecture material. ")); 		
	} 
	
	/**
	 * Cleans up the sockets and closes all the I/O channels. 
	 */ 
	private void dismissClass() {
		this.port.setDoneBroadcasting(true); 
		this.port.broadcast(new Dismissal(this.lectSentCount)); 
		this.port.closeAllOutputWriters(); 
	} 
	
	/**
	 * Handle the reception of a <code>Student</code>'s 
	 * request to ask a question. Acknowledge the 
	 * <code>HandRaise</code> and retrieve and answer the
	 * corresponding <code>Question</code>. 
	 */ 
	public void takeAQuestion() {
		// Either a HandRaise or a Question was received. 
		Message msg = this.port.receive(); 
		
		// If a hand was raised, call on THAT student and answer question. 
		if (msg instanceof HandRaise) {
			Question studentQuestion = this.callOnStudent((HandRaise) msg); 
			this.askAndAnswerQuestion(studentQuestion); 
		} 
		
		else {
			this.port.requeueMessage(msg); 
		} 
	}
	
	/**
	 * The basic logic of a classroom. The <code>Teacher</code>
	 * lectures until a <code>Student</code> raises her hand. The
	 * <code>Teacher</code> then stops lecturing and allows the
	 * <code>Student</code> to ask her question so that the entire
	 * class can hear her. Answering the question is simulated as
	 * simply continuing the lecture recitation. 
	 * @throws InterruptedException 
	 */ 
	public void run() {
		// Start the Port thread for communication. 
		new Thread(this.port).start(); 
		
		// Sleep for 10 seconds to get all students seated and ready. 
		try {
			Thread.sleep(10000); 
		} catch (InterruptedException e) {
			System.err.println("Error while teacher waited for students to enter."); 
			e.printStackTrace(); 
		} 
		
		// Announce length of today's lecture. 
		this.port.broadcast(new Message(-1, 0, 
						  "Today's lecture will contain " + 
						  this.lengthOfLecture + " sentences.")); 
		
		while(this.lectSentCount < this.lengthOfLecture) {
			// Lecture until students have a question. 			
			while (!this.port.hasMessageWaiting()) {
				this.port.broadcast(new Lecture(this.lectSentCount, "Sentence " + 
												this.lectSentCount + " of lecture.")); 
				System.out.println("Sentence " + this.lectSentCount + " of lecture."); 
				this.lectSentCount++;			
				try {
					Thread.sleep(1000); 
				} catch (InterruptedException e) {
					System.err.println("Error delaying between lecture sentences."); 
					e.printStackTrace(); 
				} 
				
				if (this.lectSentCount == this.lengthOfLecture) {
					this.dismissClass(); 
					System.out.println("Teacher leaving classroom."); 
					// Clean up everything--NIO wasn't terminating elegantly...
					System.exit(0); 
				}
			} 
			this.takeAQuestion(); 
		}
	} 
} 
