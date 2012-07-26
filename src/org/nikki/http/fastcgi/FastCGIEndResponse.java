package org.nikki.http.fastcgi;

/**
 * Represents a FCGI_REQUEST_COMPLETE response in the internal handlers
 * 
 * @author Nikki
 *
 */
public class FastCGIEndResponse {
	private int id;
	
	public FastCGIEndResponse(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
}
