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

package org.nikki.http.fastcgi;

import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.nikki.http.content.ContentHandler;
import org.nikki.http.content.HttpResponseException;
import org.nikki.http.net.HttpSession;

/**
 * A ContentHandler for FastCGI Requests
 * 
 * @author Nikki
 * 
 */
public class FastCGIContentHandler implements ContentHandler {

	/**
	 * The FastCGI Module
	 */
	private FastCGIModule module;

	/**
	 * Construct a new ContentHandler which is set to have the server ignore the
	 * first response, since the handler will respond with the finished request
	 * 
	 * @param module
	 *            The module
	 */
	public FastCGIContentHandler(FastCGIModule module) {
		this.module = module;
	}

	@Override
	public void handleRequest(HttpSession session) throws HttpResponseException {
		if (!module.handle(session)) {
			throw new HttpResponseException(HttpResponseStatus.BAD_GATEWAY);
		}
	}
}
