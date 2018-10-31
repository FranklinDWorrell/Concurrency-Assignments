package block_multiplier;

import java.io.DataInputStream; 
import java.io.DataOutputStream;
import java.io.IOException; 
import java.net.InetAddress;

import matrix.MatrixMultiple;

/**
 * A worker in the block matrix multiplication pipeline mesh. 
 * Locally builds a block of the solution and sends it to the
 * <code>Initiator</code> once the block is complete. 
 * @author Franklin D. Worrell
 * @version 11 November 2017
 */ 
public class Worker {

	private int nodeNum;
	private int localPort;
	private Connection conn;
	private int dim; 
	private int numberOfBlocks; 
	private int[][] a;
	private int[][] b;
	private int[][] c;
	private DataInputStream disInitiator;
	private DataOutputStream dosInitiator;
	private DataOutputStream dosLeft; 
	private DataOutputStream dosUp; 
	private DataInputStream disRight; 
	private DataInputStream disDown; 

	/**
	 * Creates a new instance. 
	 * @param nodeNum the unique numerical identity of this <code>Worker</code> 
	 * @param localPort the port this <code>Worker</code> will use 
	 */ 
	public Worker(int nodeNum, int localPort) {
		this.nodeNum = nodeNum;
		this.localPort = localPort;
	}

	/**
	 * Establishes connection with the <code>Initiator</code> node, gets 
	 * the connection info for adjacent <code>Worker</code> nodes from the 
	 * <code>Initiator</code>, and establishes the proper communication 
	 * streams between them. 
	 * @param initiatorIP the IP address of the <code>Initiator</code> node 
	 * @param initiatorPort the port used by the <code>Initiator</code> node 
	 */ 
	private void configure(String initiatorIP, int initiatorPort) {
		try {
			this.conn = new Connection(localPort); 
			DataIO dio = this.conn.connectIO(initiatorIP, initiatorPort); 
			this.dosInitiator = dio.getDos();  
			this.dosInitiator.writeInt(nodeNum);
			this.dosInitiator.writeUTF(Connection.getLocalNotLoopbackIP()); 
//			this.dosInitiator.writeUTF(InetAddress.getLocalHost().getHostAddress());
			this.dosInitiator.writeInt(localPort);
			this.disInitiator = dio.getDis(); 
			// Get block dimensions (square for now) and number of blocks. 
			this.dim = this.disInitiator.readInt(); 
			this.numberOfBlocks = this.disInitiator.readInt(); 
			this.a = new int[dim][dim]; 
			this.b = new int[dim][dim]; 
			this.c = new int[dim][dim]; 
			
			// Left block connection info. 
			String ipLeft = disInitiator.readUTF();	
			int portLeft = disInitiator.readInt(); 
			
			// Up block connection info. 
			String ipUp = disInitiator.readUTF(); 
			int portUp = disInitiator.readInt(); 
			
			// Right block connection info. 
			String ipRight = disInitiator.readUTF(); 
			int portRight = disInitiator.readInt();
			
			// Down block connection info. 
			String ipDown = disInitiator.readUTF(); 
			int portDown = disInitiator.readInt(); 
			
			System.out.println("L" + portLeft + " U" + portUp + " R" + 
								portRight + " D" + portDown); 
			
			// Get connections in a checkerboard fashion because of 2D wrapping. 
			int row = this.nodeNum / (int) Math.sqrt(numberOfBlocks); 
			int column = this.nodeNum % (int) Math.sqrt(numberOfBlocks); 
			System.out.println("row " + row + " and column " + column + "."); 
			if ((row % 2) != (column % 2)) { 
				System.out.println("Accepting to read from down: " + portDown + "."); 
				this.disDown = this.conn.acceptToRead(); 
				System.out.println("Connecting to write to up: " + portUp + "."); 
				this.dosUp = this.conn.connectToWrite(ipUp, portUp); 
				System.out.println("Accepting to read from right: " + portRight + "."); 
				this.disRight = this.conn.acceptToRead();  
				System.out.println("Connecting to write to left: " + portLeft + "."); 
				this.dosLeft = this.conn.connectToWrite(ipLeft, portLeft);
			} else { 
				System.out.println("Connecting to write to up: " + portUp + "."); 
				this.dosUp = this.conn.connectToWrite(ipUp, portUp); 
				System.out.println("Accepting to read from down: " + portDown + "."); 
				this.disDown = this.conn.acceptToRead(); 
				System.out.println("Connecting to write to left: " + portLeft + "."); 
				this.dosLeft = this.conn.connectToWrite(ipLeft, portLeft); 
				System.out.println("Accepting to read from right: " + portRight + "."); 
				this.disRight = this.conn.acceptToRead();	 
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} 
		System.out.println("Configuration done."); 
	}

	/**
	 * Receives this <code>Worker</code>'s initial blocks of both 
	 * multiplicands--in order--from the <code>Initiator</code> node. 
	 */ 
	private void getInitialBlocks() {
		// Get the pre-shifted block of a from Initiator 
		for (int i = 0; i < this.dim; i++) {
			for (int j = 0; j < this.dim; j++) {
				try {
					this.a[i][j] = disInitiator.readInt();
				} catch (IOException ioe) {
					System.err.println("Error receiving a[" + i + "][" + 
									   j + "] from Initiator in Worker " + 
									   this.nodeNum + "."); 
					ioe.printStackTrace();
				}
			}
		}
		
		// Get the pre-shifted block of b from Initiator
		for (int i = 0; i < this.dim; i++) {
			for (int j = 0; j < this.dim; j++) {
				try {
					this.b[i][j] = disInitiator.readInt(); 
				} catch (IOException e) {
					System.err.println("Error receiving b[" + i + "][" + 
									   j + "] from Initiator in Worker " + 
									   this.nodeNum + "."); 
					e.printStackTrace(); 
				}
			}
		}
	}

	/**
	 * Computes a partial product with the blocks currently posessed, 
	 * then sends the block of the first multiplicand to the left and 
	 * the block of the second multiplicand to the right. 
	 */ 
	private void computeShiftAndSend() {
		// Multiplication of initially received blocks. 
		this.c = MatrixMultiple.multiplyMatrices(this.a, this.b); 
		
		int sendReceivesAnticipated = (int) Math.sqrt(this.numberOfBlocks); 
		int sendReceivesCompleted = 1; 
		
		System.out.println(); 
		System.out.println("AFTER " + sendReceivesCompleted + " RECEIVES: "); 
		System.out.println("***** Matrix a: *****"); 
		MatrixMultiple.displayMatrix(this.a); 
		System.out.println("***** Matrix b: *****"); 
		MatrixMultiple.displayMatrix(this.b); 
		System.out.println("***** Matrix c: *****"); 
		MatrixMultiple.displayMatrix(this.c); 
		System.out.println(); 
		
		// Start with book's suggestion--both sends, then both receives. 
		while (sendReceivesCompleted < sendReceivesAnticipated) {
			// Send the a block left. 
			this.sendBlock(this.a, this.dosLeft); 
			// Send the b block up. 
			this.sendBlock(this.b, this.dosUp); 
			// Receive the a block from right. 
			this.receiveBlock(this.a, this.disRight); 
			// Receive the b block from down. 
			this.receiveBlock(this.b, this.disDown); 
			sendReceivesCompleted++; 
			// Perform this iteration's matrix arithmetic. 
			this.c = MatrixMultiple.addMatrices(this.c, 
						MatrixMultiple.multiplyMatrices(this.a, this.b)); 
			System.out.println(); 
			System.out.println("AFTER " + sendReceivesCompleted + " RECEIVES: "); 
			System.out.println("***** Matrix a: *****"); 
			MatrixMultiple.displayMatrix(this.a); 
			System.out.println("***** Matrix b: *****"); 
			MatrixMultiple.displayMatrix(this.b); 
			System.out.println("***** Matrix c: *****"); 
			MatrixMultiple.displayMatrix(this.c); 
			System.out.println(); 
		}
	}
	
	private void sendBlock(int[][] block, DataOutputStream dest) {
		for (int i = 0; i < block.length; i++) {
			for (int j = 0; j < block[0].length; j++) {
				try {
					dest.writeInt(block[i][j]); 
				} catch (IOException e) {
					System.err.println("Error sending block to next Worker."); 
					e.printStackTrace(); 
				}
			}
		}
	} 
	
	private void receiveBlock(int[][] dest, DataInputStream source) {
		for (int i = 0; i < dest.length; i++) {
			for (int j = 0; j < dest[0].length; j++) {
				try { 
					dest[i][j] = source.readInt(); 
				} catch (IOException e) {
					System.err.println("Error reading block from previous Worker."); 
					e.printStackTrace(); 
				}
			}
		}
	} 

	/**
	 * Sends the block of results of the multiplication by blocks 
	 * of the matrices handled by this <code>Worker</code>. 
	 */ 
	private void reportResultsToInitiator() {
		this.sendBlock(this.c, this.dosInitiator); 
	}

	/**
	 * Closes all the open I/O streams and sockets associated with this 
	 * <code>Worker</code> instance. 
	 */ 
	private void closeConnectionAndIO() {
		this.conn.close(); 
		try {
			disInitiator.close();
			dosInitiator.close();
			dosLeft.close(); 
			dosUp.close(); 
			disRight.close(); 
			disDown.close(); 			
		} catch (IOException e) {
			System.out.println("Error closing I/O streams."); 
			e.printStackTrace(); 
		} 
	} 

	public static void main(String[] args) { 
		// Validate the command-line arguments. Terminate if misused. 
		if (args.length != 4) {
			System.out.println("usage: java Worker workerID " + 
							   "worker-port-num initiator-ip " + 
							   "initiator-port-num"); 
			System.exit(1); 
		} 
		
		// Parse command-line input. 
		int workerID = Integer.parseInt(args[0]); 
		int portNum = Integer.parseInt(args[1]);
		String initIP = args[2]; 
		int initPortNum = Integer.parseInt(args[3]); 
		
		// Create and configure new Worker process. Start computations. 
		Worker worker = new Worker(workerID, portNum);
		System.out.println("Connecting to Initiator node in network."); 
		worker.configure(initIP, initPortNum); 
		System.out.println("Receiving initial blocks from Initiator node."); 
		worker.getInitialBlocks(); 
		System.out.println("Performing computations and shifts."); 
		worker.computeShiftAndSend(); 
		System.out.println("Sending results to Initiator for compilation."); 
		worker.reportResultsToInitiator(); 
		System.out.println("Closing connections."); 
		worker.closeConnectionAndIO(); 
		System.out.println("Done.");
	}
}
