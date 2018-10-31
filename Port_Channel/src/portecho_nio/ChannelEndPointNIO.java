package portecho_nio;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream; 
import java.io.IOException; 
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream; 
import java.net.InetSocketAddress;
import java.nio.ByteBuffer; 
import java.nio.channels.SocketChannel; 

import port_channel.EndPoint; 
import port_channel.Message; 

/**
 * An implementation of <code>EndPoint</code> for used with Java's NIO API 
 * to asynchronously pass messages back and forth with a <code>Port</code>. 
 * @author Franklin D. Worrell
 * @version 5 November 2017
 */ 
public class ChannelEndPointNIO extends EndPoint { 
	private ByteBuffer buffer = ByteBuffer.allocate(ChannelPortNIO.BUFFER_SIZE); 
	private SocketChannel channel; 
	
	public ChannelEndPointNIO(int nodeId, String ip, int port) {
		super(nodeId, ip, port); 
	} 

	/**
	 * Receives a <code>byte[]</code> message written to a channel by a 
	 * <code>ChannelPortNIO</code>, converts it to a <code>Message</code> 
	 * model object, and returns it. 
	 * @throws IOException
	 * @return the <code>Message</code> coming in from the <code>Port</code>
	 */ 
	@Override
	public Message receive() throws IOException {
		Message message = null; 
		try {
			buffer.clear(); 
			channel.read(buffer); 
			byte[] raw = buffer.array(); 
			ByteArrayInputStream baiStream = new ByteArrayInputStream(raw); 
			ObjectInputStream oiStream = new ObjectInputStream(baiStream); 
			message = (Message) oiStream.readObject(); 
			buffer.clear(); 
		} catch(IOException | ClassNotFoundException e) {
			System.err.println("Error receiving message in NIO end point for " + 
							   "Student " + this.getId() + "."); 
			e.printStackTrace(); 
		} 
		return message; 
	}
	
	/**
	 * Sends a <code>Message</code> to the listening <code>ChannelPortNIO</code>. 
	 * @param msg the <code>Message</code> to send to the <code>ChannelPortNIO</code>
	 */ 
	@Override
	public void send(Message msg) {
		try {
			ByteArrayOutputStream baoStream = new ByteArrayOutputStream(); 
			ObjectOutputStream ooStream = new ObjectOutputStream(baoStream); 
			ooStream.writeObject(msg); 
			byte[] message = baoStream.toByteArray(); 
			if (message.length > ChannelPortNIO.BUFFER_SIZE) {
				System.err.println("Serialized message instace too big to send."); 
				return; 
			} 
			buffer.put(message); 
			buffer.flip(); 
			this.channel.write(buffer); 
			buffer.clear(); 
		} catch (IOException e) {
			System.err.println("Error converting message for broadcasting."); 
			e.printStackTrace(); 
		} 
	}	
	
	/**
	 * Closes the <code>SocketChannel</code> that is being used for 
	 * communication with a <code>ChannelPortNIO</code>. 
	 * @throws IOException 
	 */ 
	@Override
	public void close() throws IOException {
		this.channel.close(); 
	} 
	
	/**
	 * Connect to the <code>ServerSocketChannel</code> and retain a 
	 * reference to the <code>SocketChannel</code> that will be used 
	 * for asynchronous message passing. 
	 * @throws InterruptedException
	 */ 
	@Override
	public void initialize() throws InterruptedException {
		try {
			this.channel = SocketChannel.open();
			this.channel.connect(new InetSocketAddress(this.getIp(), this.getPortNo()));
			while(!this.channel.finishConnect() ){	// Wait for connection to establish.  
				Thread.sleep(10);  
			} 
		} catch (IOException e) {
			System.err.println("Error initializing NIO end point for Student " + 
							   this.getId() + "."); 
			e.printStackTrace(); 
		} 
		Thread.sleep(1000); 
	}	
}