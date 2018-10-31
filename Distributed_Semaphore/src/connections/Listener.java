package connections;

import semaphore.Message;

import java.io.IOException; 
import java.io.ObjectInputStream; 
import java.net.SocketException;

/**
 * A <code>Thread</code> that listens at an input stream for incoming
 * messages and places them in a message queue when they arrive. The 
 * message queue is managed by the <code>ChannelPort</code> that has
 * this <code>Listener</code> instance.
 *
 * Slightly modified from code provided by the instructor. 
 *
 * @version 28 November 2017 
 */ 
public class Listener extends Thread { 
	int pId; 
	ObjectInputStream in; 
	Port port; 
	boolean done = false; 
	final int ERR_THRESHOLD = 100; 
	
	public Listener(int id, ObjectInputStream in, Port port) { 
		this.pId = id; 
		this.in = in;  
		this.port = port; 
	} 

	/**
	 * So long as the input stream is open, continuously poll it and 
	 * insert any incoming <code>Message</code> into the message queue.
	 */ 
	public void run() { 
		Message msg = null; 
		int errCnt = 0; 
		
		// So long as connection is open, listen for incoming messages. 
		while(in != null) { 
			try { 
				msg = (Message)in.readObject(); 
			} catch (SocketException e) { 
				// Exception due to dismissal. 
				if (this.port.isDoneBroadcasting()) {
					return;
				} 
				
				// Exception due to late arrival or early depature of student. 
				System.err.println(e); 
				errCnt++; 
				if (errCnt > ERR_THRESHOLD) {
					System.exit(0); 
				} 
			} catch (ClassNotFoundException | IOException e) { // Stream in inconsistent state. Terminate. 
				// Didn't need to keep listening anyway. 
				if (this.port.isDoneBroadcasting()) { 
					return;
				} 

				System.err.println("Error receiving message."); 
				e.printStackTrace(); 
				System.exit(1); 
			} 
			
			// Process valid Message. 
			if (msg != null) {
				this.port.gotMessage(msg);
			}
		}
	}
}