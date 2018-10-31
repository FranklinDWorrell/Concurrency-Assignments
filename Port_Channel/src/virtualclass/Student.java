package virtualclass;

import java.io.IOException;
import port_channel.EndPoint; 
import port_channel.ChannelEndPoint; 
import port_channel.Message; 
import port_channel.HandRaise; 
import port_channel.Question; 
import port_channel.Lecture; 
import port_channel.Dismissal; 
import portecho_nio.ChannelEndPointNIO; 

/**
 * Models a student in a classroom. Uses an <code>EndPoint</code> to establish 
 * communication with a <code>Teacher</code>'s <code>Port</code>. 
 * 
 * Modified from code provided by the instructor. 
 */ 
public class Student {
	int studentId; 
	int delayUntilQuestion; 
	int progressOfLecture;
	EndPoint ep; 

	public Student(boolean useNIO, int studentId, int delayUntilQuestion, 
				   String class_ip, int class_port) throws InterruptedException {
		this.studentId = studentId;
		this.delayUntilQuestion = delayUntilQuestion;
		this.progressOfLecture = 0; 
		
		if (useNIO) {
			this.ep = new ChannelEndPointNIO(studentId, class_ip, class_port); 
		} else {
			this.ep = new ChannelEndPoint(studentId, class_ip, class_port); 
		} 
		this.ep.initialize();
	}

	/**
	 * Listens to the <code>Teacher</code>'s lecture until the designated 
	 * point at which this <code>Student</code> is set to raise her hand 
	 * and ask a <code>Question</code>. 
	 * @throws InterruptedException 
	 * @throws IOException
	 */ 
	public void run() throws InterruptedException, IOException {
		System.out.println("Student " + this.studentId + " here: "); 
		
		while (this.progressOfLecture < this.delayUntilQuestion) {
			Message msg = this.ep.receive(); 
			System.out.println(msg.getText()); 
			if (msg instanceof Lecture) {
				this.progressOfLecture++; 
			}
		} 
		
		// Raise hand, await acknowledgement from teacher, ask random question.
		this.raiseHand(); 
		System.out.println(this.ep.receive().getText()); 
		this.askQuestion(); 
		
		// Continue listening until class is dismissed. 
		while (true) {
			Message message = this.ep.receive(); 
			System.out.println(message.getText()); 
			if (message instanceof Dismissal) {
				System.out.println("Class was dismissed. Student " + 
								   this.studentId + " leaving classroom."); 
				break; 
			}
		}

		// Close I/O channels
		try {
			this.ep.close(); 
		} catch (IOException e) {
			System.out.println("Error closing ChannelEndPoint as " + 
							   "Student terminated."); 
			e.printStackTrace(); 
		} 
	} 
	
	/**
	 * Informs the <code>Teacher</code> that this 
	 * <code>Student</code> has a question that she wants
	 * to ask.
	 */ 
	private void raiseHand() {
		this.ep.send(new HandRaise(this.studentId, this.progressOfLecture)); 
	} 
	
	/**
	 * Asks the <code>Teacher</code> a question.
	 */ 
	public void askQuestion() {
		ep.send(new Question(this.studentId, this.progressOfLecture)); 
	}
	
	public static void main(String[] args) throws IOException, InterruptedException {
		// Validate command-line arguments. 
		if (args.length != 5) { 
			System.out.println("usage: java Student S[ocket]|N[IO] stuId " + 
							   "delay-til-question classroom-ip classroom-port");
			System.exit(1); 
		} 
		
		// Parse command-line arguments. 
		boolean useNIO = args[0].equals("N"); 
 		int stuId = Integer.parseInt(args[1]); 
 		int timeUntilQuestion = Integer.parseInt(args[2]); 
		String ip = args[3]; 
		int portNo = Integer.parseInt(args[4]); 
		
		// Create new student and start them listening/questioning. 
		Student student = new Student(useNIO, stuId, timeUntilQuestion, ip, portNo); 
		student.run(); 
	}
}
