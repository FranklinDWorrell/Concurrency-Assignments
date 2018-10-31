package portecho_nio;

import port_channel.Port; 
import port_channel.Message; 

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress; 
import java.net.ServerSocket; 
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException; 
import java.nio.channels.SelectionKey; 
import java.nio.channels.Selector; 
import java.nio.channels.ServerSocketChannel; 
import java.nio.channels.SocketChannel; 
import java.util.Iterator; 
import java.util.Set; 

/**
 * An implementation of <code>Port</code> that utilizes Java's NIO 
 * API for asynchronous communication with client <code>ChannelEndPointNIO</code>s. 
 * @author Franklin D. Worrell
 * @version 6 November 2017
 */ 
public class ChannelPortNIO extends Port {
	public static final int BUFFER_SIZE = 262144; 
	private boolean channelsClosed = false; 
	private int ports[]; 
	private SocketChannel channels[]; 
	private ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE); 
	private Selector selector; 
	private ServerSocketChannel ssc; 
	private ServerSocket socket; 
	
	public ChannelPortNIO(int portNo, int networkSize) {
		super(portNo, networkSize); 
		this.channels = new SocketChannel[networkSize]; 
		this.selector = null; 
		this.ssc = null; 
		this.socket = null; 
	}	

	/**
	 * Opens a <code>ServerSocketChannel</code> at <code>this.portNo</code>
	 * and accepts incoming connection requestions until the number of 
	 * <code>SocketChannel</code>s established equals <code>this.networkSize</code>
	 * before control of the <code>Selector</code> is passed off to the
	 * <code>ListenerNIO</code> thread. 
	 */ 
	public void initialize() { 
		// Create and configure the ServerSocketChannel. 
		try {
			selector = Selector.open(); 
			ssc = ServerSocketChannel.open(); 
			ssc.configureBlocking(false); 
			socket = ssc.socket(); 
			InetSocketAddress address = new InetSocketAddress(this.getPortNo());
			socket.bind(address); 
			SelectionKey key = ssc.register(selector, SelectionKey.OP_ACCEPT);
		} catch (IOException e) {
			System.err.println("Configuration of ServerSocket for NIO failed."); 
			e.printStackTrace(); 
			System.exit(1); 
		} 
		
		// Accept connections from all the EndPoints. 
		int endPointsFound = 0;
		while (endPointsFound < this.getNetworkSize()) { 
			try {
				int foundThisIteration = selector.select(); 
				Set<SelectionKey> selectedKeys = selector.selectedKeys(); 
				Iterator<SelectionKey> iterator = selectedKeys.iterator(); 
				
				while (iterator.hasNext()) {
					SelectionKey nextKey = iterator.next(); 
					// Only attempted connections are being processed at this time.
					if (nextKey.isAcceptable()) { 
						System.out.println("Accepting a new connection."); 
						SocketChannel channel = ssc.accept(); 
						channel.configureBlocking(false); 
						SelectionKey newReadKey = channel.register(this.selector, SelectionKey.OP_READ); 
						channels[endPointsFound] = channel; 
						
						iterator.remove(); 
						endPointsFound++; 
					} 
				} 
			} catch (ClosedChannelException e) {
				System.err.println("A client terminated during port initialization."); 
				e.printStackTrace(); 
			} catch (IOException e) { 
				e.printStackTrace(); 
			}
		} 
	} 

	/**
	 * Configures the <code>Port</code> and starts the <code>Thread</code> 
	 * that will listen for incoming <code>Message</code>s from all the 
	 * <code>SocketChannel</code>s. Since this thread will no longer
	 * interfere with the <code>Selector</code> after establishing all 
	 * connections with the <code>EndPoints</code>s, it is safe to pass
	 * the reference to the listener thread. 
	 */ 
	@Override 
	public void run() { 
		this.initialize(); 
		// Create a new worker thread that catches messages from Channels.
		(new ListenerNIO(this, this.selector)).start(); 
	}
	
	/**
	 * Converts a <code>Message</code> to a <code>byte[]</code> and 
	 * then writes that <code>byte[]</code> to each connected 
	 * <code>SocketChannel</code>. 
	 * @param msg the <code>Message</code> to send to every <code>ChannelEndPointNIO</code>
	 */ 
	@Override
	public synchronized void broadcast(Message msg) {
		try {
			this.buffer.clear(); 
			ByteArrayOutputStream baoStream = new ByteArrayOutputStream(); 
			ObjectOutputStream ooStream = new ObjectOutputStream(baoStream); 
			ooStream.writeObject(msg); 
			byte[] message = baoStream.toByteArray(); 
			if (message.length > BUFFER_SIZE) {
				System.err.println("Serialized message instace too big to send."); 
				return; 
			} 
			this.buffer.put(message); 
			this.buffer.flip(); 
			
			for (SocketChannel channel : this.channels) {
				channel.write(this.buffer); 
				this.buffer.rewind(); 
			} 

		} catch (IOException e) {
			System.err.println("Error converting message for broadcasting."); 
			e.printStackTrace(); 
		} 
	} 
	
	/**
	 * Closes each of the <code>SocketChannel</code>s used to communicate 
	 * with the <code>ChannelEndPointNIO</code>s. 
	 */ 
	@Override
	public void closeAllOutputWriters() {
		for (SocketChannel channel : this.channels) {
			try {
				channel.close(); 
			} catch (IOException e) {
				System.err.println("Error closing a client's channel."); 
				e.printStackTrace(); 
			} 
		}
	} 
}
