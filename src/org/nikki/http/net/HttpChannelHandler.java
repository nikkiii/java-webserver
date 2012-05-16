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

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.nikki.http.HttpServer;

/**
 * A basic netty channel handler
 * 
 * @author Nikki
 * 
 */
public class HttpChannelHandler extends SimpleChannelUpstreamHandler {

	/**
	 * The http session
	 */
	private HttpSession session;

	/**
	 * Creates a new HTTPChannelHandler.
	 * 
	 * @param server
	 */
	public HttpChannelHandler(HttpServer server) {
		this.session = new HttpSession(server);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
		if (ctx.getChannel().isConnected()) {
			ctx.getChannel().close();
		}
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		Object message = e.getMessage();
		if (message instanceof HttpRequest) {
			session.handleRequest(ctx, (HttpRequest) message);
		}
	}

}
