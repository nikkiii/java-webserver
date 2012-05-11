package org.nikki.http.module;

import org.nikki.http.impl.ContentHandler;

/**
 * A module for extensions that serve files, such as FastCGI -> PHP and SCGI
 * 
 * @author Nikki
 *
 */
public abstract class ContentModule extends ServerModule {
	
	/**
	 * @see org.nikki.http.impl.ContentManager.registerExtension(extension, handler)
	 */
	public void registerExtension(String extension, ContentHandler handler) {
		server.getContentManager().registerExtension(extension, handler);
	}
}
