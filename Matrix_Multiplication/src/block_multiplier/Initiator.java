package block_multiplier;

import java.io.DataInputStream; 
import java.io.DataOutputStream; 
import java.io.IOException; 

import matrix.MatrixMultiple; 

/**
 * This process coordinates the distributed matrix multiplication by 
 * blocks application. 
 * @author Franklin D. Worrell
 * @version 10 November 2017
 */ 
public class Initiator { 
	
	private Connection conn; 
	private int dim; 
	private int blockDim; 
	private int[][] a; 
	private int[][] b; 
	private int[][] c; 
	private int numNodes; 
	private DataInputStream[] disWorkers;
	private DataOutputStream[] dosWorkers; 
	
	/**
	 * Initializes the two <code>int[][]</code> multiple matrices 
	 * of the size specified. 
	 * @param n the dimension of the square multiple matrices
	 * @param numNodes the number of <code>Worker</code> processes available
	 */ 
	public Initiator(int n, int numNodes) { 
		this.dim = n; 
		this.a = MatrixMultiple.createDisplayMatrix(n); 
		this.b = MatrixMultiple.createDisplayMatrix(n); 
		this.c = new int[n][n]; 
		this.numNodes = numNodes; 
		this.blockDim = n / (int) Math.sqrt(numNodes); 
	}
	
	/**
	 * Establish connections to all the Workers in the network 
	 * and connect them in the mesh of pipelines. 
	 * @param portNum the port number used for communication to <code>this</code>
	 */ 
 	private void configure(int portNum) { 
		try { 
			// Create map of communication network. 
			this.conn = new Connection(portNum); 
			this.disWorkers = new DataInputStream[numNodes]; 
			this.dosWorkers = new DataOutputStream[numNodes];
			String[] ips = new String[numNodes]; 
			int[] ports = new int[numNodes]; 
			
			// Establish communications with each node in the network. 
			for (int i = 0; i < numNodes; i++ ) { 
				DataIO dio = conn.acceptConnect(); 
				DataInputStream dis = dio.getDis(); 
				int nodeNum = dis.readInt(); 			//get worker ID
				ips[nodeNum] = dis.readUTF(); 			//get worker ip
				ports[nodeNum] = dis.readInt();  		//get worker port #
				disWorkers[nodeNum] = dis; 
				dosWorkers[nodeNum] = dio.getDos(); 	//the stream to worker ID
				dosWorkers[nodeNum].writeInt(this.blockDim); //assign matrix dimension (height) 
				dosWorkers[nodeNum].writeInt(this.numNodes); 
			} 
			
			// Establish connections for flow of data in pipelines. 
			int numWPerRow = (int) Math.sqrt(this.numNodes); 
			for (int w = 0; w < numNodes; w++) { 
				int row = w / numWPerRow; 
				int col = w % numWPerRow; 
				
				// Shift (send) to left Worker's ip. 
				int left = ((w + numWPerRow - 1) % numWPerRow) + (row * numWPerRow); 
				dosWorkers[w].writeUTF(ips[left]); 
				dosWorkers[w].writeInt(ports[left]); 
				
				// Shift (send) to up Worker's ip. 
				int up = (w - numWPerRow + numNodes) % numNodes; 
				dosWorkers[w].writeUTF(ips[up]); 
				dosWorkers[w].writeInt(ports[up]); 
				
				// Receive from right Worker's ip. 
				int right = ((w + numWPerRow + 1) % numWPerRow) + (row * numWPerRow); 
				dosWorkers[w].writeUTF(ips[right]); 
				dosWorkers[w].writeInt(ports[right]); 
				
				// Receive from down Worker's ip. 
				int down = (w + numWPerRow) % numNodes; 
				dosWorkers[w].writeUTF(ips[down]); 
				dosWorkers[w].writeInt(ports[down]); 
				
				System.out.println(); 
				System.out.println("Config for Worker " + w + ":"); 
				System.out.println("L" + left + " U" + up + " R" + right + " D" + down); 
				System.out.println(); 
			}
		} catch (IOException ioe) { 
			System.out.println("error: Initiator assigning neighbor infor.");  
			ioe.printStackTrace(); 
		} 
	}
	
	/**
	 * Distributes the appropriate blocks of each multiple to the 
	 * appropriate <code>Worker</code>. 
	 */ 
	private void distribute() { 
		int[][] shiftedA = this.getInitiallyShiftedA(); 
		int[][] shiftedB = this.getInitiallyShiftedB(); 
		
		System.out.println("***** Initial shift of a: *****"); 
		MatrixMultiple.displayMatrix(shiftedA); 
		System.out.println("***** Initial shift of b: *****"); 
		MatrixMultiple.displayMatrix(shiftedB); 
	
		// Send out the blocks of matrix a. 
		System.out.println("Sending blocks of matrix a to Workers."); 
		for (int w = 0; w < this.numNodes; w++) {
			this.distributeProperBlock(shiftedA, w); 
		} 
		System.out.println("Finished sending blocks of matrix a to Workers."); 
		
		// Send out the blocks of matrix b. 
		System.out.println("Sending blocks of matrix b to Workers."); 
		for (int w = 0; w < this.numNodes; w++) {
			this.distributeProperBlock(shiftedB, w); 
		} 
		System.out.println("Finished sending blocks of matrix b to Workers."); 		
		System.out.println("Finished distributing blocks to Workers."); 
		System.out.println(); 
	}
	
	/**
	 * Sends the appropriate block of the given <code>int[][]</code> 
	 * matrix to the specified <code>Worker</code> node in the network. 
	 * @param matrix the <code>int[][]</code> to be distributed by block 
	 * @param node the node that needs its block
	 */ 
	private void distributeProperBlock(int[][] matrix, int node) {
		int startRow = (node / (int) Math.sqrt(this.numNodes)) * this.blockDim; 
		int startCol = (node % (int) Math.sqrt(this.numNodes)) * this.blockDim; 
		for (int i = startRow; i < startRow + this.blockDim; i++) {
			for (int j = startCol; j < startCol + this.blockDim; j++) {
				try {
					this.dosWorkers[node].writeInt(matrix[i][j]); 
				} catch (IOException e) {
					System.err.println("Error sending an initial block to " + 
									   "Worker " + node + " for processing."); 
					e.printStackTrace(); 
				}
			}
		}
	} 

	/**
	 * Collect the results from each <code>Worker</code> and 
	 * insert these results into the matrix being used for the
	 * product of the multiplication.
	 */ 
	private void collectProductBlocksFromWorkers() {
		for (int w = 0; w < this.numNodes; w++) { 
			int startRow = (w / (int) Math.sqrt(this.numNodes)) * this.blockDim; 
			int startCol = (w % (int) Math.sqrt(this.numNodes)) * this.blockDim; 
			for (int i = startRow; i < (startRow + this.blockDim); i++) {
				for (int j = startCol; j < (startCol + this.blockDim); j++) {
					try {
						this.c[i][j] = disWorkers[w].readInt(); 
					} catch (IOException e) {
						System.err.println("Error getting result from Worker " + 
										   w + " for c[" + i + "][" + j + "]"); 
						e.printStackTrace(); 
					}
				}
			}
		} 
	} 

	private void close() {
		this.conn.close(); 
		for (DataInputStream inputStream : disWorkers) {
			try {
				inputStream.close(); 
			} catch (IOException e) {
				System.err.println("Error closing input stream."); 
				e.printStackTrace(); 
			} 
		} 
		for (DataOutputStream outputStream : dosWorkers) {
			try {
				outputStream.close(); 
			} catch (IOException e) {
				System.err.println("Error closing output stream."); 
				e.printStackTrace(); 
			} 
		}
	} 
	
	/**
	 * Performs the initial leftward shifting of values in the 
	 * first multiple matrix, <code>this.a</code>. 
	 * @return a leftward shifted version of the first multiple
	 */ 
	private int[][] getInitiallyShiftedA() {
		int[][] shiftedA = new int[this.dim][this.dim]; 
		for (int i = 0; i < this.dim; i++) {
			for (int j = 0; j < this.dim; j++) {
				shiftedA[i][j] = a[i][(j + ((i / this.blockDim) + 1) * this.blockDim ) % this.dim]; 
			}
		} 
		return shiftedA; 
	} 
	
	/**
	 * Performs the initial upward shifting of values in the 
	 * second multiple matrix, <code>this.b</code>. 
	 * @return a leftward shifted version of the second multiple
	 */ 
	private int[][] getInitiallyShiftedB() {
		int[][] shiftedB = new int[this.dim][this.dim]; 
		for (int i = 0; i < this.dim; i++) {
			for (int j = 0; j < this.dim; j++) {
				shiftedB[i][j] = b[(i + ((j / this.blockDim) + 1) * this.blockDim) % this.dim][j]; 
			} 
		} 
		return shiftedB;
	} 
	
	/**
	 * Returns the product of <code>this.a</code> and <code>this.b</code> 
	 * for use in proving correctness of the result of the distributed
	 * application.
	 * @return the product of the two <code>int[][]</code> multiples, calculated locally
	 */ 
	private int[][] getTestProduct() {
		return MatrixMultiple.multiplyMatrices(this.a, this.b); 
	} 
	
	/**
	 * Returns the first <code>int[][]</code> multiple being used by 
	 * the distributed matrix multiplication by blocks program. 
	 * @return the first matrix multiple
	 */ 
	public int[][] getA() {
		return this.a; 
	}
	
	/**
	 * Returns the second <code>int[][]</code> multiple being used by 
	 * the distributed matrix multiplication by blocks program. 
	 * @return the second matrix multiple
	 */ 
	public int[][] getB() {
		return this.b; 
	} 
	
	/** 
	 * Returns the dimension of the square matrices being multiplied. 
	 * @return the dimension of the square multiples and product
	 */ 
	public int getDimension() {
		return this.dim; 
	} 

	/**
	 * Returns the actual product of the multiplication of the two 
	 * matrices as calculated by the distributed application. 
	 * @return the product of the two <code>int[][]</code> multiples
	 */ 
	public int[][] getC() {
		return this.c; 
	} 
	
	/**
	 * Prints the amount of time that passed between phases of algorithm. 
	 * @param startTime the beginning of the computation time in milliseconds 
	 * @param phaseOneEnd the ending time of computation phase one in milliseconds
	 * @param endTime the ending of the computation time in milliseconds 
	 */ 
	public void printComputationTimes(long startTime, long phaseOneEnd, long endTime) {
		System.out.println("Total time for Phases 1 and 2: " + 
						   ((double) (endTime - startTime) / 1000.0) + 
						   " seconds."); 
		System.out.println("Phase 1 required " + 
						   ((double) (phaseOneEnd - startTime) / 1000.0) + 
						   " seconds."); 
		System.out.println("Phase 2 required " + 
						   ((double) (endTime - phaseOneEnd) / 1000.0) + 
						   " seconds."); 
	}
	
	/**
	 * Prints the two multiples and the product matrices in a formatted
	 * fashion for confirmation of output. 
	 */ 
	public void printMultiplesAndProduct() {
		System.out.println("#####     Matrix A     #####"); 
		MatrixMultiple.displayMatrix(this.getA()); 
		System.out.println(); 
		System.out.println("#####     Matrix B     #####"); 
		MatrixMultiple.displayMatrix(this.getB()); 
		System.out.println(); 
		System.out.println("##### Product Matrix C  #####"); 
		MatrixMultiple.displayMatrix(this.getC()); 
		System.out.println(); 
	}
	
	/**
	 * @require Math.floor(Math.sqrt(args[1])) == Math.ceil(Math.sqrt(args[1])) 
	 * @require (args[0] % Math.sqrt(args[1])) == 0
	 */ 
	public static void main(String[] args) { 
		// Validate command-line arguments. Terminate is misused. 
		if (args.length != 3) {
			System.out.println("usage: java Initiator maxtrix-dim " + 
							   "number-nodes initiator-port-num"); 
			System.exit(1); 
		} 
		
		// Parse command-line input. 
		int matrixSize = Integer.parseInt(args[0]); 
		int numNodes = Integer.parseInt(args[1]); 
		int portNo = Integer.parseInt(args[2]); 
		
		// Create coordinate process and configure connections. 
		System.out.println("Creating Initiator instance"); 
		Initiator initiator = new Initiator(matrixSize, numNodes); 
		
		// Phase 1: Configuration of pipeline mesh and distribution of data. 
		System.out.println("Beginning Phase 1: Establish connections, distribute data."); 
		long startTime = System.currentTimeMillis(); 
		initiator.configure(portNo); 
		initiator.distribute(); 
		long phaseOneEnd = System.currentTimeMillis(); 
		
		// Phase 2: Workers perform calculations and report results back. 
		System.out.println("Beginning Phase 2: Waiting for response from Workers."); 
		initiator.collectProductBlocksFromWorkers(); 
		long endTime = System.currentTimeMillis(); 
		
		System.out.println("Closing open connections."); 
		initiator.close(); 

		// Confirm that calculated product is correct and print results. 
		int[][] testProduct = initiator.getTestProduct(); 
		
		if (MatrixMultiple.compareMatrices(testProduct, initiator.getC())) {
			System.out.println("Results from Workers match expected."); 
		} else {
			System.out.println("RESULTS FROM WORKERS DID NOT MATCH EXPECTED!"); 
		} 

		initiator.printMultiplesAndProduct(); 
		System.out.println("***** ANTICIPATED PRODUCT *****"); 
		MatrixMultiple.displayMatrix(testProduct); 
		System.out.println(); 
		initiator.printComputationTimes(startTime, phaseOneEnd, endTime);
		System.out.println("Done.");
	}
}
