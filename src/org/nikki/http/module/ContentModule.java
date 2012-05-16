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

package org.nikki.http.module;

import org.nikki.http.content.ContentHandler;

/**
 * A module for extensions that serve files, such as FastCGI -> PHP and SCGI
 * 
 * @author Nikki
 * 
 */
public abstract class ContentModule extends ServerModule {

	/**
	 * @see org.nikki.http.impl.ContentManager.registerExtension(extension,
	 *      handler)
	 */
	public void registerExtension(String extension, ContentHandler handler) {
		server.getContentManager().registerExtension(extension, handler);
	}
}
