package org.nikki.http.fastcgi;

import org.jboss.netty.handler.codec.http.HttpResponse;
import org.nikki.http.HttpSession;
import org.nikki.http.impl.ContentHandler;

/**
 * A ContentHandler for FastCGI Requests
 * 
 * @author Nikki
 *
 */
public class FastCGIContentHandler extends ContentHandler {

	/**
	 * The FastCGI Module
	 */
	private FastCGIModule module;

	/**
	 * Construct a new ContentHandler which is set to have the server ignore the first response, since the handler will respond with the finished request
	 * @param module
	 * 			The module
	 */
	public FastCGIContentHandler(FastCGIModule module) {
		super(true);
		this.module = module;
	}
	
	@Override
	public HttpResponse handleRequest(HttpSession session) {
		return module.handle(session);
	}
}
