package port_channel;

import java.util.Random; 

/**
 * Models a student's question for the virtual classroom
 * program. The text of the question is chosen at random
 * from a small set of possible questions. 
 * @author Franklin D. Worrell
 * @version 30 October 2017
 */ 
public class Question extends Message {
	private static String[] possibleQuestions = {
		"Who first had this idea?", 
		"What was the long-term significance of this piece?", 
		"When was this piece first published?", 
		"Where was the author living when the piece was written?", 
		"How did this piece influence later thinkers?", 
		"Will this material appear on the exam?", 
		"Would this piece be an appropriate paper topic?",
		"Should we study the secondary literature on this piece?"}; 
	private static Random random = new Random(); 
	
	/**
	 * Initializes a new question message and randomly selects
	 * the text of the question from the possible options. 
	 * @param studentId the student who asked the question 
	 * @param timestamp the time at which the student asked
	 */ 
	public Question(int studentId, int timestamp) {
		super(studentId, timestamp, 
			  possibleQuestions[random.nextInt(possibleQuestions.length)]); 
	} 
} 