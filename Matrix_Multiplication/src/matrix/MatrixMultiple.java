package matrix;

/** 
 * A utility class used to generate a matrix, multiply two
 * matrices, and compare matrices for equality. 
 *
 * This class was implemented by the instructor and refined by me. 
 */ 
public class MatrixMultiple {

	/** 
	 * Creates a square matrix populated with values that make checking 
	 * matrix arithmetic simpler. 
	 * @param n the desired dimension of the square matrix 
	 * @return an n X n <code>int[][]</code> matrix 
	 */ 
	public static int[][] createDisplayMatrix(int n) {
		int[][] matrix = new int[n][n];
		int up = (int)Math.pow(10, (int)Math.log10(n)+1); 
		for (int row = 1; row <= n; row++) {
			for (int col = 1; col <= n; col++) {
				matrix[row - 1][col - 1] = row * up + col;
			}
		}
		return matrix; 
	}
	
	/**
	 * Creates an identity matrix of the specified size. 
	 * @param n the size of the desired identity matrix
	 * @return an n X n identity matrix 
	 */ 
	public static int[][] createIdentityMatrix(int n) {
		int[][] matrix = new int[n][n];
		for (int row = 0; row < n; row++) {
			for (int col = 0; col < n; col++) {
				matrix[row][col] = 0;
			}
			matrix[row][row] = 1; 
		}
		return matrix; 
	}
	
	/**
	 * Creates a square matrix populated with random <code>int</code>
	 * values. 
	 * @param n the desired dimension of the square matrix 
	 * @return the <code>int[][]</code> square matrix 
	 */ 
	public static int[][] createRandomMatrix(int n) {
		int[][] matrix = new int[n][n];
		for (int row = 0; row < n; row++) {
			for (int col = 0; col < n; col++) {
				matrix[row][col] = (int)(Math.random()*1000);
			}
		}
		return matrix; 
	}
	
	/**
	 * Prints the provided <code>int[][]</code> matrix to stdout. 
	 * @require (mat.length == mat[0].length) && (mat.length <= 660) 
	 * @param mat the matrix to display 
	 */ 
	public static void displayMatrix(int[][] mat) {
		int n = mat.length; 
		int m = mat[0].length; 
		if (n <= 660) {
			int digit = (int) Math.log10(n)*2+3;
			for (int row = 0; row < n; row++) {
				for (int col = 0; col < m; col++) {
					String numStr = String.format("%"+digit+"d", mat[row][col]);
					System.out.print(numStr);
				}
				System.out.println();
			}
		} else {
			System.out.println("The matrix is too big to display on screen.");
		}
	}
	
	/** 
	 * Display an n by n matrix with elements of no more than d digits 
	 * to stdout. 
	 * @param mat the <code>int[][]</code> to display 
	 * @param d the maximum number of digits of each <code>int</code> element 
	 */ 
	public static void displayMatrix(int[][] mat, int d) {
		int n = mat.length; 
		int m = mat[0].length; 
		for (int row = 0; row < n; row++) {
			for (int col = 0; col < m; col++) {
				String numStr = String.format("%"+(d+2)+"d", mat[row][col]);
				System.out.print(numStr);
			}
			System.out.println();
		}
	}

	/**
	 * Adds two matrices. 
	 * @require (a.length == b.length) && (a[0].length == b[0].length)
	 * @param a one addend matrix
	 * @param b the other added matrix
	 * @return a new int[][] sum matrix
	 */ 
	public static int[][] addMatrices(int[][] a, int[][] b) {
		int n = a.length;
		int[][] c = new int[n][n]; 
		for (int row = 0; row < n; row++) { 
			for (int col = 0; col < n; col++) {
				c[row][col] = a[row][col] + b[row][col]; 
			}
		} 
		return c; 
	}

	/**
	 * Multiplies two matrices. 
	 * @require a[0].length == b.length
	 * @param a one multiple matrix
	 * @param b the other multiple matrix
	 * @return a new int[][] product matrix
	 */ 
	public static int[][] multiplyMatrices(int[][] a, int[][] b) {
		int n = a.length;
		int[][] c = new int[n][n]; 
		for (int row = 0; row < n; row++) { 
			for (int col = 0; col < n; col++) {
				c[row][col] = 0; 
				for (int i = 0; i < n; i++) {
					c[row][col] = c[row][col] + a[row][i] * b[i][col];
				}
			}
		} 
		return c; 
	}
	
	/**
	 * Compares two matrices for equality. Iterates through each 
	 * matrix and individually compares corresponding elements of
	 * each. 
	 * @require (a.length == a[0].length) && (b.length == b[0].length)
	 * @param a one matrix to compare 
	 * @param b the other matrix for comparison
	 * @return whether the matrices are equal
	 */ 
	public static boolean compareMatrices(int[][] a, int[][] b) {
		int n = a.length;
		boolean result = true; 
		for (int row = 0; row < n; row++) { 
			for (int col = 0; col < n; col++) {
				if (a[row][col] != b[row][col]) {
					result = false; 
					System.out.println("row="+row+" col="+col + ":"+a[row][col]+"<-->"+b[row][col]); 
				}
			}
		} 
		return result; 
	}
}
