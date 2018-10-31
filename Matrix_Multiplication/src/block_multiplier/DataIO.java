package block_multiplier;

import java.io.DataInputStream; 
import java.io.DataOutputStream; 

/**
 * Wraps a <code>DataInputStream</code> and a <code>DataOutputStream</code>
 * into a single object for simplicity in passing references. 
 * 
 * Implemented by instructor. 
 */ 
public class DataIO {
	DataInputStream dis; 
	DataOutputStream dos; 
	
	public DataIO(DataInputStream dis, DataOutputStream dos) { 
		this.dis = dis; 
		this.dos = dos; 
	}

	public DataInputStream getDis() {
		return dis;
	}

	public void setDis(DataInputStream dis) {
		this.dis = dis;
	}

	public DataOutputStream getDos() {
		return dos;
	}

	public void setDos(DataOutputStream dos) {
		this.dos = dos;
	} 
}
