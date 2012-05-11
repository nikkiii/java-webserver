package org.nikki.http.impl;

import java.util.HashMap;

import org.jboss.netty.handler.codec.http.HttpRequest;

/**
 * A class to manage Webserver files and content
 * 
 * @author Nikki
 *
 */
public class ContentManager {
	
	/**
	 * The basic handler which will serve from the document root, whether it's an HTML/web related file or a download
	 */
	private ContentHandler defaultHandler = new StaticFileContentHandler();
	
	/**
	 * The map with extensions -> handlers
	 */
	private HashMap<String, ContentHandler> handlerExtensions = new HashMap<String, ContentHandler>();
	
	/**
	 * TODO Allow mime types of local files once document root system is finished
	 */
	private HashMap<String, ContentHandler> handlerMimeTypes  = new HashMap<String, ContentHandler>();
	
	/**
	 * Construct a new Content Manager
	 */
	public ContentManager() {
	}
	
	/**
	 * Register an extension to a handler
	 * @param extension
	 * 			The extension
	 * @param handler
	 * 			The handler
	 */
	public void registerExtension(String extension, ContentHandler handler) {
		handlerExtensions.put(extension, handler);
	}
	
	/**
	 * Register a mime type to a handler
	 * @param mimeType
	 * 			The mime type
	 * @param handler
	 * 			The handler
	 */
	public void registerMimeType(String mimeType, ContentHandler handler) {
		handlerMimeTypes.put(mimeType, handler);
	}

	/**
	 * Get the handler for the request by using the URI
	 * 
	 * TODO allow mime type lookup on local files
	 * 
	 * @param request
	 * 			The request
	 * @return
	 * 			The handler
	 */
	public ContentHandler getHandlerFor(HttpRequest request) {
		String uri = request.getUri();
		if(uri.indexOf('?') != -1) {
			uri = uri.substring(0, uri.indexOf('?'));
		}
		String extension = uri.substring(uri.lastIndexOf('.')+1);
		if(handlerExtensions.containsKey(extension)) {
			return handlerExtensions.get(extension);
		} else {
			return defaultHandler;
		}
	}
}
