package org.nikki.http.fastcgi;

import org.jboss.netty.buffer.ChannelBuffer;

/**
 * A finished FastCGI Response
 * 
 * @author Nikki
 *
 */
public class FastCGIResponse {
	
	/**
	 * The original request id
	 */
	private int id;
	
	/**
	 * The completed data of the request
	 */
	private ChannelBuffer data;
	
	/**
	 * Construct a new response
	 * @param id
	 * 			The request id
	 * @param data
	 * 			The response
	 */
	public FastCGIResponse(int id, ChannelBuffer data) {
		this.id = id;
		this.data = data;
	}

	/**
	 * Get the request id
	 * @return
	 * 		The request id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Get the data buffer
	 * @return
	 * 		The ChannelBuffer containing the response data
	 */
	public ChannelBuffer getData() {
		return data;
	}
}
