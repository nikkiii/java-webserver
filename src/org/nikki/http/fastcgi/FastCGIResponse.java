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
	 * 
	 * @param id
	 *            The request id
	 * @param data
	 *            The response
	 */
	public FastCGIResponse(int id, ChannelBuffer data) {
		this.id = id;
		this.data = data;
	}

	/**
	 * Get the data buffer
	 * 
	 * @return The ChannelBuffer containing the response data
	 */
	public ChannelBuffer getData() {
		return data;
	}

	/**
	 * Get the request id
	 * 
	 * @return The request id
	 */
	public int getId() {
		return id;
	}
}
