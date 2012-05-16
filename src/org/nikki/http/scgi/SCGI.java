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

package org.nikki.http.scgi;

/*
 Copyright (c) 2008 ArtemGr

 Permission to use, copy, modify, and/or distribute this software for any
 purpose with or without fee is hereby granted, provided that the above
 copyright notice and this permission notice appear in all copies.

 THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * SCGI connector.<br>
 * Version: 1.0<br>
 * Home page: http://gist.github.com/38425 See also:
 * http://en.wikipedia.org/wiki/SCGI
 */
public class SCGI {
	public static class SCGIException extends IOException {
		private static final long serialVersionUID = 1L;

		public SCGIException(String message) {
			super(message);
		}
	}

	/** Used to decode the headers. */
	public static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");

	// Taken from nTorrent
	public static String make(HashMap<String, String> header, String body) {
		String res = "CONTENT_LENGTH\0" + (body != null ? body.length() : 0)
				+ "\0SCGI\0" + "1\0";
		if (header != null) {
			for (Map.Entry<String, String> entry : header.entrySet())
				res += entry.getKey() + '\0' + entry.getValue() + '\0';
		}
		String size = new Integer(res.getBytes().length) + ":";
		res += "," + body;
		return size + res;
	}

	/**
	 * Read the <a href="http://python.ca/scgi/protocol.txt">SCGI</a> request
	 * headers.<br>
	 * After the headers had been loaded, you can read the body of the request
	 * manually from the same {@code input} stream:
	 * 
	 * <pre>
	 * // Load the SCGI headers.
	 * Socket clientSocket = socket.accept();
	 * BufferedInputStream bis = new BufferedInputStream(
	 * 		clientSocket.getInputStream(), 4096);
	 * HashMap&lt;String, String&gt; env = SCGI.parse(bis);
	 * // Read the body of the request.
	 * bis.read(new byte[Integer.parseInt(env.get(&quot;CONTENT_LENGTH&quot;))]);
	 * </pre>
	 * 
	 * @param input
	 *            an efficient (buffered) input stream.
	 * @return strings passed via the SCGI request.
	 */
	public static HashMap<String, String> parse(InputStream input)
			throws IOException {
		StringBuilder lengthString = new StringBuilder(12);
		String headers = "";
		for (;;) {
			char ch = (char) input.read();
			if (ch >= '0' && ch <= '9') {
				lengthString.append(ch);
			} else if (ch == ':') {
				int length = Integer.parseInt(lengthString.toString());
				byte[] headersBuf = new byte[length];
				int read = input.read(headersBuf);
				if (read != headersBuf.length)
					throw new SCGIException("Couldn't read all the headers ("
							+ length + ").");
				headers = ISO_8859_1.decode(ByteBuffer.wrap(headersBuf))
						.toString();
				if (input.read() != ',')
					throw new SCGIException("Wrong SCGI header length: "
							+ lengthString);
				break;
			} else {
				lengthString.append(ch);
				throw new SCGIException("Wrong SCGI header length: "
						+ lengthString);
			}
		}
		HashMap<String, String> env = new HashMap<String, String>();
		while (headers.length() != 0) {
			int sep1 = headers.indexOf(0);
			int sep2 = headers.indexOf(0, sep1 + 1);
			env.put(headers.substring(0, sep1),
					headers.substring(sep1 + 1, sep2));
			headers = headers.substring(sep2 + 1);
		}
		return env;
	}
}