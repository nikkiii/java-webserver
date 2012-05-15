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

package org.nikki.http.fastcgi.util;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

/**
 * A small class containing simple methods for parsing headers/etc
 * 
 * @author Nikki
 *
 */
public class FastCGIUtil {
	
	/**
	 * Parse the headers, and only the headers, off the FastCGI Response buffer
	 * @param res
	 * 			The outgoing response object
	 * @param buffer
	 * 			The FastCGI Response buffer
	 */
	public static void parseHeaders(HttpResponse res, ChannelBuffer buffer) {
		String line = "", key = "", value = "";
		while (buffer.readable()) {
			line = readLine(buffer);
			if (line.length() == 0) {
				return;
			}
			if (line.indexOf(':') == -1) {
				key = line;
			} else {
				key = line.substring(0, line.indexOf(':'));
				value = line.substring(line.indexOf(':')+1).trim();
			}
			
			if (key.equalsIgnoreCase("status")) {
				res.setStatus(HttpResponseStatus.valueOf(Integer.parseInt(value
						.substring(0, value.indexOf(' ')))));
			} else if (key.startsWith("http") || key.startsWith("HTTP")) {
				// Standard header
			} else if (key.equalsIgnoreCase("location")) {
				res.addHeader(key, value);
			} else
				res.addHeader(key, value);
		}
	}

	/**
	 * Read a line from the specified ChannelBuffer
	 * @param buffer
	 * 			The buffer to read from
	 * @return
	 * 			The finished line
	 */
	public static String readLine(ChannelBuffer buffer) {
		StringBuilder lineBuf = new StringBuilder();
		for (;;) {
			int b = buffer.readByte();
			if (b < 0 || b == '\n') {
				break;
			}

			lineBuf.append((char) b);
		}

		if(lineBuf.charAt(0) == '\r') {
			return lineBuf.substring(1);
		}
		
		while (lineBuf.charAt(lineBuf.length() - 1) == '\r') {
			lineBuf.setLength(lineBuf.length() - 1);
		}

		return lineBuf.toString();
	}
}
