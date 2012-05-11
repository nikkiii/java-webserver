package org.nikki.http.fastcgi;

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
				System.out.println("Status: "+value);
				res.setStatus(HttpResponseStatus.valueOf(Integer.parseInt(value
						.substring(0, value.indexOf(' ')))));
			} else if (key.startsWith("http") || key.startsWith("HTTP")) {
				// Standard header
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
