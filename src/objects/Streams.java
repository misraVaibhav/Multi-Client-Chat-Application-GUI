/**
 * 
 */
package objects;


import java.io.*;

/**
 * @author vaibhav
 *
 */
public class Streams {

	private ObjectInputStream in;
	private ObjectOutputStream out;

	public Streams (ObjectInputStream in, ObjectOutputStream out) {
		this.out = out;
		this.in = in;
	}

	public ObjectInputStream getIS() {
		return this.in;
	}

	public ObjectOutputStream getOS() {
		return this.out;
	}
}