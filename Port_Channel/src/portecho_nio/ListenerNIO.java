package portecho_nio; 

import java.io.ByteArrayInputStream; 
import java.io.IOException; 
import java.io.ObjectInputStream; 
import java.nio.ByteBuffer; 
import java.nio.channels.NotYetConnectedException; 
import java.nio.channels.SelectionKey; 
import java.nio.channels.Selector; 
import java.nio.channels.SocketChannel; 
import java.util.Iterator; 
import java.util.Set; 
import port_channel.HandRaise; 
import port_channel.Message; 
import port_channel.Question; 

/**
 * A <code>Thread</code> that listens to each <code>SocketChannel</code> 
 * in an array and processes any write requests that have been received.
 * @author Franklin D. Worrell
 * @version 6 November 2017
 */ 
public class ListenerNIO extends Thread { 
	ChannelPortNIO port; 
	Selector selector; 
	ByteBuffer readBuffer; 
	
	public ListenerNIO(ChannelPortNIO port, Selector selector) {
		this.port = port; 
		this.selector = selector; 
		this.readBuffer = ByteBuffer.allocate(ChannelPortNIO.BUFFER_SIZE); 
	} 
	
	/**
	 * Prints to <code>System.out</code> if a received <code>Message</code> 
	 * was a <code>HandRaise</code> or a <code>Question</code>.
	 * @param msq the previously received <code>Message</code> 
	 */ 
	private void reportMessage(Message msg) {
		if (msg instanceof HandRaise) {
			System.out.println("A hand was raised by Student " + 
							   msg.getStudentId() + " at second " + 
							   msg.getTimestamp() + " of lecture."); 
		}
		else if (msg instanceof Question) {
			System.out.println("A question was asked by Student " + 
							   msg.getStudentId() + " at second " + 
							   msg.getTimestamp() + " of lecture."); 
		}
	} 
	
	/**
	 * Until the <code>ChannelPortNIO</code> signals that communication
	 * should end, listens for incoming messages from the previously
	 * established <code>SocketChannel</code>s, converts the 
	 * <code>byte[]</code> message into a <code>Message</code> object, 
	 * and sends the <code>Message</code> to the <code>ChannelPortNIO</code> 
	 * to be inserted into the message queue. 
	 */ 
	@Override 
	public void run() {
		while (!this.port.isDoneBroadcasting()) {
			try { 
				int num = this.selector.select(); 
			} catch (IOException e) {
				System.err.println("Listener's call to selector failed."); 
				e.printStackTrace(); 
			} 
			Set<SelectionKey> keys = this.selector.selectedKeys(); 
			Iterator<SelectionKey> iterator = keys.iterator(); 
			
			while (iterator.hasNext()) {
				SelectionKey key = iterator.next(); 
				if (key.isReadable()) { 
					SocketChannel sc = (SocketChannel) key.channel();
					
					try {
						int bytesRead = sc.read(readBuffer); 
						if (bytesRead > 0) { 
							// Convert input to model Message object. 
							byte[] raw = readBuffer.array(); 
							ByteArrayInputStream baiStream = new ByteArrayInputStream(raw); 
							ObjectInputStream oiStream = new ObjectInputStream(baiStream); 
							Message message = (Message) oiStream.readObject(); 
							readBuffer.clear(); 
							
							// Log message and add it to the port's queue. 
							if (message != null) {
								this.reportMessage(message); 
								this.port.gotMessage(message); 
							} 
						}
					} catch (NotYetConnectedException | 
							 IOException | 
							 ClassNotFoundException e) {
						System.err.println("Error retrieving and/or enqueing message."); 
						e.printStackTrace(); 
					} 
					
					iterator.remove(); 
				} 
			} 
		} 
	}
}