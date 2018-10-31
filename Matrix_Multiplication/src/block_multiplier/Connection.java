package block_multiplier;

import java.io.DataInputStream; 
import java.io.DataOutputStream; 
import java.io.IOException; 
import java.net.InetAddress; 
import java.net.InterfaceAddress; 
import java.net.NetworkInterface; 
import java.net.ServerSocket; 
import java.net.Socket; 
import java.net.SocketException; 
import java.util.Collections; 
import java.util.List; 

/**
 * A class that abstracts away the specifics of connections between 
 * both the <code>Initiator</code> and the <code>Worker</code> nodes 
 * and among the <code>Worker</code> nodes. 
 * 
 * Implemented by instructor. I made minor revisions. 
 */ 
public class Connection {
	
	private static final int MAX_RETRIES = 100; 
	
	private String name; 
	private int port; 
	private ServerSocket servsoc; 
	
	public Connection(int portNum) { 
		this.port = portNum; 
		try {
			servsoc = new ServerSocket(port); 
		} catch (IOException ioe) {
			System.err.println("Could not listen on port: " + port + ", " + ioe);  
			ioe.printStackTrace(); 
			System.exit(1); 
		}
	} 
	
	public DataIO acceptConnect() {
		int retryCount = 0; 
		DataIO dio = null;  
		while (true) {
			try {
				Socket sc = servsoc.accept(); 
				DataInputStream dis = new DataInputStream(sc.getInputStream()); 
				DataOutputStream dos = new DataOutputStream(sc.getOutputStream()); 
				dio = new DataIO(dis, dos); 
				break; 
			} catch (IOException ioe) {
				System.err.println("Failed to connect to worker node."); 
				ioe.printStackTrace(); 
				retryCount++; 
				try {
					Thread.sleep(200); 
				} catch (InterruptedException e) {
					System.err.println("Try again."); 
				}
			} 
			
			if (retryCount >= MAX_RETRIES) {
				this.close(); 
				break; 
			}
		} 
		return dio;
	} 
	
	public DataInputStream acceptToRead() {
		int retryCount = 0; 
		DataInputStream dis = null;  
		while (true) {
			try {
				Socket sc = servsoc.accept(); 
				dis = new DataInputStream(sc.getInputStream()); 
				break; 
			} catch (IOException ioe) {
				System.err.println("Failed to connect for reading from worker node.");
				ioe.printStackTrace(); 
				retryCount++; 
				try {
					Thread.sleep(200); 
				} catch (InterruptedException e) {
					System.err.println("Try again."); 
				}
			}
			if (retryCount >= MAX_RETRIES) {
				this.close(); 
				break; 
			}
		}
		return dis;
	} 
	
	public DataOutputStream acceptToWrite() {
		int retryCount = 0; 
		DataOutputStream dos = null;  
		while (true) {
			try {
				System.out.println("About to attempt to accept to write."); 
				Socket sc = servsoc.accept(); 
				System.out.println("Completed call to accept to write.");
				System.out.println("Creating DOS."); 
				dos = new DataOutputStream(sc.getOutputStream()); 
				System.out.println("Created DOS."); 
				break; 
			} catch (IOException ioe) {
				System.err.println("Failed to connect for writing to worker node.");
				ioe.printStackTrace(); 
				retryCount++; 
				try {
					Thread.sleep(200); 
				} catch (InterruptedException e) {
					System.err.println("Try again."); 
				}
			}
			if (retryCount >= MAX_RETRIES) {
				this.close(); 
				break; 
			}
		}
		return dos;
	} 

	public DataIO connectIO(String ip, int port) {
		int retryCount = 0; 
		DataIO dio = null;
		while (true) {
			try {
				Socket sc = new Socket(ip, port);
				DataInputStream dis = new DataInputStream(sc.getInputStream());
				DataOutputStream dos = new DataOutputStream(sc.getOutputStream()); 
				dio = new DataIO(dis, dos); 
				break; 
			} catch (IOException ioe) {
				System.err.println("Failed to connect to ip=" + ip + ", port="	+ port);
				ioe.printStackTrace(); 
				retryCount++; 
				try {
					Thread.sleep(200); 
				} catch (InterruptedException e) {
					System.err.println("Try again."); 
				}
			}
			if (retryCount >= MAX_RETRIES) {
				this.close(); 
				break; 
			}
		}
		return dio;
	}
	
	public DataOutputStream connectToWrite(String ip, int port) {
		int retryCount = 0; 
		DataOutputStream dos = null;
		while (true) {
			try {
				Socket sc = new Socket(ip, port);
				dos = new DataOutputStream(sc.getOutputStream()); 
				break; 
			} catch (IOException ioe) {
				System.err.println("Failed to connect to write to ip=" + ip + ", port="	+ port);
				ioe.printStackTrace(); 
				retryCount++; 
				try {
					Thread.sleep(200);	
				} catch (InterruptedException e) {
					System.err.println("Try again."); 
				}
			}
			if (retryCount >= MAX_RETRIES) {
				this.close(); 
				break; 
			}
		}
		return dos;
	}
	
	public DataInputStream connectToRead(String ip, int port) {
		int retryCount = 0; 
		DataInputStream dis = null;
		while (true) {
			try {
				Socket sc = new Socket(ip, port);
				dis = new DataInputStream(sc.getInputStream()); 
				break; 
			} catch (IOException ioe) {
				System.err.println("Failed to connect to read to ip=" + ip + ", port="	+ port);
				ioe.printStackTrace(); 
				retryCount++; 
				try {
					Thread.sleep(200); 
				} catch (InterruptedException e) {
					System.err.println("Try again."); 
				}
			}
			if (retryCount >= MAX_RETRIES) {
				this.close(); 
				break; 
			}
		}
		return dis;
	} 

	public void close() {
		try { 
			this.servsoc.close(); 
		} catch (IOException e) {
			System.err.println("Error closing ServerSocket."); 
			e.printStackTrace(); 
		} 
	} 
	
	public static String getLocalNotLoopbackIP() {
		try {
			List<NetworkInterface> nis = Collections.list(NetworkInterface.getNetworkInterfaces()); 
			for (NetworkInterface ni : nis) {
			  if (!ni.isLoopback() && ni.isUp() && ni.getHardwareAddress() != null) {
				for (InterfaceAddress ia : ni.getInterfaceAddresses()) {
				  if (ia.getBroadcast() != null) { 
					return ia.getAddress().getHostAddress();
				  }
				}
			  }
			}
		} catch (SocketException e) {
			e.printStackTrace(); 
		} 
		
		return null; 
	} 
}
