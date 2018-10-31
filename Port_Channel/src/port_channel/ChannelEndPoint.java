package port_channel;

import java.io.IOException; 
import java.io.ObjectInputStream; 
import java.io.ObjectOutputStream; 
import java.net.Socket; 
import java.net.SocketException; 
import java.net.UnknownHostException; 

/**
 * An <code>EndPoint</code> implementation for use with Java sockets. 
 *
 * Slightly modified from code provided by the instructor.
 */ 
public class ChannelEndPoint extends EndPoint { 
	ObjectOutputStream out; 
	ObjectInputStream in; 
	Socket socket = null; 
	
	public ChannelEndPoint(int nodeId, String ip, int port) {
		super(nodeId, ip, port); 
	}

	/**
	 * Create a new <code>Socket</code> connected to the <code>Port</code> 
	 * and initialize the input and output streams used for message passing. 
	 * @throws InterruptedException
	 */ 
	@Override
	public void initialize() throws InterruptedException { 
		while (this.socket == null) {
			try {
				this.socket = new Socket(this.getIp(), this.getPortNo()); 
				System.out.println("Got a socket."); 
				this.in = new ObjectInputStream(this.socket.getInputStream()); 
				this.out = new ObjectOutputStream(this.socket.getOutputStream());
			} catch (UnknownHostException e) {
				System.err.println("Don't know about the server."); 
			} catch (IOException e) {
				System.err.println("Couldn't get I/O for the connection to server.");
			} 
			Thread.sleep(1000); 
		}
	} 
	
	/**
	 * Returns a message received from the <code>ChannelPort</code> server. 
	 * @throws IOException 
	 * @return the <code>Message</code> received from the <code>ChannelPort</code> 
	 */ 
	@Override
	public Message receive() throws IOException { 
		Message result = null; 
		try {
			result = (Message) this.in.readObject(); 
		} catch (ClassNotFoundException e) {
			System.err.println("Error deserializing message."); 
			e.printStackTrace(); 
		} 
		return result; 
	} 
	
	/**
	 * Sends the provided <code>Message</code> to the <code>ChannelPort</code> 
	 * that listening for incoming messages. 
	 * @param msg the <code>Message</code> to send to the <code>Port</code>
	 */ 
	@Override
	public void send(Message msg) { 
		try { 
			this.out.writeObject(msg);
		} catch (SocketException se) { 
			System.err.println("Socket error when attempting to raise " + 
							   "or ask a question."); 
			se.printStackTrace(); 
			System.exit(1);
		} catch (IOException ioe) {
			System.err.println("Error raising hand or asking question."); 
			ioe.printStackTrace(); 
		}
	}
	
	/**
	 * Closes the input and output streams as well as the <code>Socket</code> 
	 * that were being used for communication with the <code>ChannelPort</code>. 
	 * @throws IOException 
	 */ 
	@Override
	public void close() throws IOException { 
		try { 
			System.out.println("Walking out of classroom..."); 
		} catch (Exception e) { 
			e.printStackTrace();
		} finally {
			this.in.close();
			this.out.close();
			this.socket.close();
		}
	}
}