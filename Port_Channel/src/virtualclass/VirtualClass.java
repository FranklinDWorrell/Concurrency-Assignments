package virtualclass;

import java.io.IOException;

import port_channel.Port; 
import port_channel.ChannelPort; 
import portecho_nio.ChannelPortNIO; 

/**
 * Configures a <code>Teacher</code> set to use either Java sockets or 
 * Java's NIO API for interacting with a set number of <code>Student</code>s 
 * in a class. Creates a <code>Port</code> using the specified communication
 * API and passes that <code>Port</code> to the <code>Teacher</code> to use.
 *
 * Slightly modified from code provided by the instructor. 
 */ 
public class VirtualClass {
	private Teacher teacher; 
	private Port port; 
	
	public VirtualClass(boolean useNIO, int portNo, int numStudent, int lengthOfLecture) { 
		// Use the user specified channel port implementation
		if (useNIO) {
			this.port = new ChannelPortNIO(portNo, numStudent);  
		} else {
			this.port = new ChannelPort(portNo, numStudent);  
		}
		
		teacher = new Teacher(this.port, lengthOfLecture); 
	}
	
	/**
	 * Returns a reference to the <code>Teacher</code> lecturing in 
	 * this <code>VirtualClass</code> instance. 
	 * @return the <code>Teacher</code> lecturing during this class
	 */ 
	private Teacher getTeacher() {
		return this.teacher; 
	} 

	public static void main(String[] args) throws IOException, InterruptedException { 
		// Validate command-line arguments. Terminate if incorrect. 
		if (args.length != 4) {
			System.out.println("usage: java VirtualClass S[ocket]|N[IO] " + 
							   "port-number " + "number-of-students " + 
							   "length-of-lecture"); 
			System.exit(1); 
		}
		
		// Parse the command-line arguments. 
		boolean useNIO = args[0].equals("N"); 
		int portNo = Integer.parseInt(args[1]); 
		int n = Integer.parseInt(args[2]); 
		int length = Integer.parseInt(args[3]); 
		
		// Get the classroom set up and start the lecture. 
		VirtualClass vc = new VirtualClass(useNIO, portNo, n, length); 
		new Thread(vc.getTeacher()).start(); 
		System.out.println("Lecture will start once all students are seated."); 
	}
} 
