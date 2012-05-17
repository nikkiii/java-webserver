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

package org.nikki.http.net;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.nikki.http.HttpServer;

/**
 * Represents an HTTP Session/Request
 * 
 * @author Nikki
 * 
 */
public class HttpSession {

	/**
	 * The server instance
	 */
	private HttpServer server;

	/**
	 * The request object
	 */
	private HttpRequest request;

	/**
	 * The request's channel
	 */
	private Channel channel;

	/**
	 * Creates an HTTP Session.
	 */
	public HttpSession(HttpServer server) {
		this.server = server;
	}

	/**
	 * Get the channel
	 * 
	 * @return The session's channel
	 */
	public Channel getChannel() {
		return channel;
	}

	/**
	 * Get the HTTP Request
	 * 
	 * @return The request
	 */
	public HttpRequest getRequest() {
		return request;
	}

	/**
	 * Get the server this session belongs to
	 * 
	 * @return The server
	 */
	public HttpServer getServer() {
		return server;
	}

	/**
	 * Handle the request
	 * 
	 * @param ctx
	 *            The ChannelHandlerContext of the connectino
	 * @param request
	 *            The HTTP Request
	 */
	public void handleRequest(ChannelHandlerContext ctx, HttpRequest request) {
		this.request = request;
		this.channel = ctx.getChannel();

		server.handleRequest(this);
	}

	/**
	 * Sends the http response.
	 * 
	 * @param res
	 *            The http response.
	 */
	public void sendHttpResponse(HttpResponse res) {
		sendHttpResponse(res, true);
	}
	
	/**
	 * Sends an HTTP Response with a flag whether to close or keep alive
	 * @param res
	 * 			The response
	 * @param close
	 * 			The flag
	 */
	public void sendHttpResponse(HttpResponse res, boolean close) {
		if(!res.containsHeader("Server")) {
			res.setHeader("Server", HttpServer.SERVER_SOFTWARE + " "
				+ HttpServer.SERVER_VERSION);
		}
		ChannelFuture future = channel.write(res);
		if(close) {
			future.addListener(ChannelFutureListener.CLOSE);
		}
	}
}
