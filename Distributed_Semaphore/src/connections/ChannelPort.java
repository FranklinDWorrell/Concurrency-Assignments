package connections;

import semaphore.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * The <code>connections.Port</code> implementation for use with Java sockets.
 * Modified from code provided by the instructor. 
 * @version 30 November 2017 
 */
public class ChannelPort extends Port {
    ObjectInputStream[] ins; 
	ObjectOutputStream[] outs;
    Listener[] listeners; 
//	boolean insSet = false; 

    public ChannelPort(int portNo, int networkSize) {
        super(portNo, networkSize);
        this.outs = new ObjectOutputStream[networkSize];
        this.listeners = new Listener[this.networkSize];
    }

    /**
     * Creates a <code>ServerSocket</code> listening at the port number
     * passed to the constructor. Waits for each of the anticipated
     * <code>ChannelEndPoint</code>s to connected and creates the threads
     * that will individually listen for incoming messages.
     */
    public void initialize() {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(this.portNo);
        } catch (IOException ioe) {
            System.out.println("Error initializing server socket.");
            ioe.printStackTrace();
        }
        for (int j = 0; j < networkSize; j++) {
            try {
                Socket clientSocket = serverSocket.accept(); 	    // not part of communication
                outs[j] = new ObjectOutputStream(clientSocket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
                listeners[j] = new Listener(j, in, this);
            } catch (IOException ioe) {
                System.err.println("Failed in connection for j=" + j);
                ioe.printStackTrace();
                System.exit(-1);
            }
        }
        System.out.println("Completed connections with other semaphore helpers.");
    }

    /**
     * Configures the <code>ServerSocket</code> and then starts up all of
     * the threads that listen for incoming messages from the connected
     * <code>EndPoint</code>s.
     */
    @Override
    public void run() {
		// Open server socket and await EndPoint connections. 
        initialize();
		
		// Wait for IO to be resolved. 
/*		while (!this.insSet) {
			try {
				Thread.sleep(500); 
			} catch (InterruptedException e) {
				System.err.println("Error waiting for resolution of IO streams."); 
				e.printStackTrace(); 
			}
		}
*/		
        // Start each listener thread.
        for (int j = 0; j < networkSize; j++) {
            listeners[j].start();
        }
    }

    /**
     * Sends a message to every listening <code>ChannelEndPoint</code>.
     * @param msg the message to broadcast to every <code>Student</code>
     */
    @Override
    public synchronized void broadcast(Message msg) {
        for (int j = 0; j < outs.length; j++) {
            try {
                outs[j].writeObject(msg);
            } catch (SocketException e) {
                System.err.println("Error in socket connection while " +
                        "broadcasting to DisSemHelper " + j + ".");
                e.printStackTrace();
            } catch (IOException e) {
                System.err.println("Error broadcasting to DisSemHelper " + j + ".");
                e.printStackTrace();
            }
        }
    }

    /**
     * Closes each of the output streams used to communication with
     * the <code>ChannelEndPoint</code>s.
     */
    @Override
    public void closeAllOutputWriters() {
        for (int j = 0; j < outs.length; j++) {
            try {
                this.outs[j].close();
            } catch (IOException e) {
                System.err.println("Error closing " + j + "'s ObjectOutputStream.");
            }
        }
    }
	
}
