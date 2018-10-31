package semaphore;

/**
 * An implementation of a distributed semaphore.
 * @author Franklin D. Worrell
 * @version 1 December 2017
 */
public class DistributedSemaphore {
    
	private int nodeID; 
	private String ip; 
	private int helperPort; 
	private String initiatorIP; 
	private int initiatorPort; 
	private DisSemHelper disSemHelper;

    public DistributedSemaphore(int nodeID, String ip, int helperPort, String initiatorIP, 
								int initiatorPort) {
		this.nodeID = nodeID; 
		this.ip = ip; 
		this.helperPort = helperPort; 
		this.initiatorIP = initiatorIP; 
		this.initiatorPort = initiatorPort; 
	}

    /**
     * Acquires the semaphore. Blocks until semaphore acquired. 
     */
    public void p() {
			System.out.println("DS entering p() to so resource can be acquired."); 
			this.disSemHelper.acquireSemaphore(); 
			while (!this.disSemHelper.hasSemaphore()) { /* Wait */ }
			System.out.println("DS exiting p() so bid can be placed."); 
    }

    /**
     * Releases the semaphore.
     */
    public void v() {
        this.disSemHelper.releaseSemaphore();
    }
	
	/**
	 * Creates and starts the necessary <code>DisSemHelper</code> thread. 
	 */ 
	public void startHelper() {
        this.disSemHelper = new DisSemHelper(this, this.nodeID, this.ip, 
				this.helperPort, this.initiatorIP, this.initiatorPort);
		this.disSemHelper.init(); 		
	} 

}
