package org.nikki.http.content;

import org.jboss.netty.handler.codec.http.HttpResponseStatus;

/**
 * An exception used to specify that the handler had a problem, and that the server should return an error response
 * 
 * @author Nikki
 *
 */
public class HttpResponseException extends Exception {

	/**
	 * The serial uid
	 */
	private static final long serialVersionUID = -548587047264963177L;

	/**
	 * The status
	 */
	private HttpResponseStatus status;

	/**
	 * Construct a new exception
	 * @param status
	 * 			The HTTP Status to return
	 */
	public HttpResponseException(HttpResponseStatus status) {
		this.status = status;
	}

	/**
	 * Get the status
	 * @return
	 * 		The HTTP Status
	 */
	public HttpResponseStatus getStatus() {
		return status;
	}
}
