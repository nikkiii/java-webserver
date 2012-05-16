/**
 * JavaHttpd, the flexible Java webserver
 * Copyright (C) 2012 Nikki <nikki@nikkii.us>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.nikki.http.content;

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
	 * The basic handler which will serve from the document root, whether it's
	 * an HTML/web related file or a download
	 */
	private ContentHandler defaultHandler = new StaticFileContentHandler();

	/**
	 * The handler which will construct directory listings
	 */
	private ContentHandler directoryListHandler = new DirectoryListingContentHandler();

	/**
	 * The map with extensions -> handlers
	 */
	private HashMap<String, ContentHandler> handlerExtensions = new HashMap<String, ContentHandler>();

	/**
	 * TODO Allow mime types of local files once document root system is
	 * finished
	 */
	private HashMap<String, ContentHandler> handlerMimeTypes = new HashMap<String, ContentHandler>();

	/**
	 * Construct a new Content Manager
	 */
	public ContentManager() {
	}

	/**
	 * Get the content handler for Directory Listings
	 * 
	 * @return The Directory listing handler
	 */
	public ContentHandler getDirectoryListHandler() {
		return directoryListHandler;
	}

	/**
	 * Get the handler for the request by using the URI
	 * 
	 * TODO allow mime type lookup on local files
	 * 
	 * @param request
	 *            The request
	 * @return The handler
	 */
	public ContentHandler getHandlerFor(HttpRequest request) {
		String uri = request.getUri();
		if (uri.indexOf('?') != -1) {
			uri = uri.substring(0, uri.indexOf('?'));
		}
		String extension = uri.substring(uri.lastIndexOf('.') + 1);
		if (handlerExtensions.containsKey(extension)) {
			return handlerExtensions.get(extension);
		} else {
			return defaultHandler;
		}
	}

	/**
	 * Register an extension to a handler
	 * 
	 * @param extension
	 *            The extension
	 * @param handler
	 *            The handler
	 */
	public void registerExtension(String extension, ContentHandler handler) {
		handlerExtensions.put(extension, handler);
	}

	/**
	 * Register a mime type to a handler
	 * 
	 * @param mimeType
	 *            The mime type
	 * @param handler
	 *            The handler
	 */
	public void registerMimeType(String mimeType, ContentHandler handler) {
		handlerMimeTypes.put(mimeType, handler);
	}
}
