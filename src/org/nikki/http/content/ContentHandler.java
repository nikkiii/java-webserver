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

import org.nikki.http.net.HttpSession;

/**
 * A class representing a server content handler 
 * A content handler can be any of the following: 
 * - A simple static file reader 
 * - A FastCGI backend
 * - A request handler for Modules
 * 
 * @author Nikki
 * 
 */
public interface ContentHandler {

	/**
	 * Handle a request
	 * 
	 * @param session
	 *            The session which initiated the request
	 * @return The response, or null
	 */
	public void handleRequest(HttpSession session) throws HttpResponseException;
}
