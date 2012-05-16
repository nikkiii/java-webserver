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

import static org.nikki.http.fastcgi.FastCGIConstants.FCGI_BEGIN_REQUEST;
import static org.nikki.http.fastcgi.FastCGIConstants.FCGI_KEEP_CONN;
import static org.nikki.http.fastcgi.FastCGIConstants.FCGI_PARAMS;
import static org.nikki.http.fastcgi.FastCGIConstants.FCGI_RESPONDER;
import static org.nikki.http.fastcgi.FastCGIConstants.FCGI_STDIN;
import static org.nikki.http.fastcgi.FastCGIConstants.FCGI_VERSION;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.Map.Entry;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.nikki.http.HttpServer;
import org.nikki.http.net.HttpSession;

/**
 * Represents a FastCGI Request packet
 * 
 * @link http://www.fastcgi.com/devkit/doc/fcgi-spec.html
 * 
 * @author Nikki
 * 
 */
public class FastCGIRequest {

	/**
	 * The output buffer
	 */
	private ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();

	/**
	 * The session
	 */
	private HttpSession session;

	/**
	 * The request
	 */
	private HttpRequest request;

	/**
	 * The request id
	 */
	private int requestId;

	/**
	 * Construct a new request instance
	 * 
	 * @param session
	 *            The session
	 */
	public FastCGIRequest(HttpSession session, int requestId) {
		this.session = session;
		this.request = session.getRequest();
		this.requestId = requestId;
	}

	/**
	 * Add a header field, basic Name-Value pair Not my own work, even from the
	 * doc, taken from a FastCGI Servlet and converted to use ChannelBuffers
	 * 
	 * @param key
	 *            The key
	 * @param value
	 *            The value
	 */
	public void addHeader(String key, String value) {
		if (value != null) {

			int keyLen = key.length();
			int valLen = value.length();

			int len = keyLen + valLen;

			if (keyLen < 0x80)
				len += 1;
			else
				len += 4;

			if (valLen < 0x80)
				len += 1;
			else
				len += 4;

			writeHeader(FCGI_PARAMS, len);

			if (keyLen < 0x80)
				buffer.writeByte(keyLen);
			else {
				buffer.writeByte(0x80 | (keyLen >> 24));
				buffer.writeByte(keyLen >> 16);
				buffer.writeByte(keyLen >> 8);
				buffer.writeByte(keyLen);
			}

			if (valLen < 0x80)
				buffer.writeByte(valLen);
			else {
				buffer.writeByte(0x80 | (valLen >> 24));
				buffer.writeByte(valLen >> 16);
				buffer.writeByte(valLen >> 8);
				buffer.writeByte(valLen);
			}

			buffer.writeBytes(key.getBytes());
			buffer.writeBytes(value.getBytes());
		}
	}

	/**
	 * Construct the request
	 * 
	 * @return The request id
	 */
	public void construct() {
		// The request id is for our records, just so we don't mix them up... if
		// we had a blocking request system, we wouldn't need it
		writeHeader(FCGI_BEGIN_REQUEST, 8);

		buffer.writeShort(FCGI_RESPONDER);
		buffer.writeByte(FCGI_KEEP_CONN);
		for (int i = 0; i < 5; i++)
			buffer.writeByte(0);

		// Request params
		addHeader("REQUEST_URI", request.getUri());
		addHeader("REQUEST_METHOD", request.getMethod().getName());

		// Address
		InetSocketAddress remote = (InetSocketAddress) session.getChannel()
				.getRemoteAddress();
		addHeader("REMOTE_ADDR", remote.getAddress().getHostAddress());
		addHeader("REMOTE_PORT", Integer.toString(remote.getPort()));
		addHeader("REMOTE_HOST", remote.getHostName());

		// Server info
		addHeader("SERVER_SOFTWARE", HttpServer.SERVER_SOFTWARE + " "
				+ HttpServer.SERVER_VERSION);
		addHeader("SERVER_NAME", HttpServer.SERVER_SOFTWARE);

		addHeader("SERVER_REQUEST_ID", Integer.toString(requestId));

		long contentLength = HttpHeaders.getContentLength(request);

		addHeader("GATEWAY_INTERFACE", "CGI/1.1");
		addHeader("CONTENT_LENGTH", String.valueOf(contentLength));

		String fileUri = request.getUri().substring(0);
		String queryString = "";
		if (fileUri.indexOf('?') != -1) {
			queryString = fileUri.substring(fileUri.indexOf('?') + 1);
			fileUri = fileUri.substring(0, fileUri.indexOf('?'));
		}

		// TODO for different document roots/non testing cases, the path won't
		// be like this
		File path = new File(session.getServer().getDocumentRoot(), fileUri);

		addHeader("SCRIPT_FILENAME", path.getAbsolutePath().replace('\\', '/'));
		addHeader("SCRIPT_NAME", fileUri);

		addHeader("QUERY_STRING", queryString);

		addHeader("DOCUMENT_ROOT", session.getServer().getDocumentRoot()
				.getAbsolutePath().replace('\\', '/'));

		for (Entry<String, String> entry : request.getHeaders()) {
			String key = entry.getKey();
			String value = entry.getValue();
			if (key.equalsIgnoreCase("content-length"))
				addHeader("CONTENT_LENGTH", value);
			else if (key.equalsIgnoreCase("content-type"))
				addHeader("CONTENT_TYPE", value);
			else
				addHeader("HTTP_" + key.replace('-', '_').toUpperCase(), value);
		}

		writeHeader(FCGI_PARAMS, 0);

		// For POST requests, this is sent as the data, after seeing this I
		// really understood how POSTdata worked
		if (contentLength > 0) {
			writeHeader(FCGI_STDIN, (int) contentLength);
			buffer.writeBytes(request.getContent());
			writeHeader(FCGI_STDIN, 0);
		}
	}

	/**
	 * Return the data buffer
	 * 
	 * @return The data buffer
	 */
	public ChannelBuffer toBuffer() {
		return buffer;
	}

	/**
	 * Write a FastCGI Header
	 * 
	 * @param type
	 * @param length
	 */
	public void writeHeader(int type, int length) {
		buffer.writeByte(FCGI_VERSION);
		buffer.writeByte(type);
		buffer.writeShort(requestId);
		buffer.writeShort(length);
		buffer.writeByte(0);
		buffer.writeByte(0);
	}
}
