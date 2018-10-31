package semaphore; 

import connections.ChannelPort; 

/**
 * Listens to what the <code>ChannelPort</code> being used by its
 * <code>DisSemHelper</code> receives and passes these messages 
 * back to its <code>DisSemHelper</code> for processing. 
 * @author Franklin D. Worrell
 * @version 29 November 2017 
 */ 
public class HelperListener implements Runnable {
	
	private DisSemHelper disSemHelper; 
	private ChannelPort channelPort; 
	
	public HelperListener(DisSemHelper disSemHelper, ChannelPort channelPort) {
		this.disSemHelper = disSemHelper; 
		this.channelPort = channelPort; 
	} 
	
	@Override 
	public void run() {
		while (true) {
			Message msg = this.channelPort.receive(); 
			this.disSemHelper.handleMessage(msg); 
		} 
	} 
	
} 
