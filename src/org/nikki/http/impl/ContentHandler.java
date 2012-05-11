package org.nikki.http.impl;

import org.jboss.netty.handler.codec.http.HttpResponse;
import org.nikki.http.HttpSession;

/**
 * A class representing a server content handler
 * A content handler can be any of the following:
 * - A simple static file reader
 * - A request handler for Modules
 * 
 * @author Nikki
 *
 */
public abstract class ContentHandler {
	
	/**
	 * This may be named weirdly, but this specifies if the module will write the request itself, or if the server should do it
	 */
	private boolean async = false;
	
	/**
	 * Blank, either not async or setAsync will be called
	 */
	public ContentHandler() {
		
	}
	
	/**
	 * Construct a new handler with the async flag set
	 * @param async
	 * 			true if the handler will respond by itself to the session
	 */
	public ContentHandler(boolean async) {
		this.async = async;
	}

	/**
	 * Handle a request
	 * @param session
	 * 			The session which initiated the request
	 * @return
	 * 			The response, or null
	 */
	public abstract HttpResponse handleRequest(HttpSession session);
	
	/**
	 * Set the async flag
	 * @param async
	 * 			...
	 */
	public void setAsync(boolean async) {
		this.async = async;
	}
	
	/**
	 * Get the async flag
	 * @return
	 * 		The boolean representing the mode
	 */
	public boolean isAsync() {
		return async;
	}
}
