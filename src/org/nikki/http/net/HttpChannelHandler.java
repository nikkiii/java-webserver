package org.nikki.http.net;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.nikki.http.HttpServer;
import org.nikki.http.HttpSession;

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
	 * @param server
	 */
	public HttpChannelHandler(HttpServer server) {
		this.session = new HttpSession(server);
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
		if(ctx.getChannel().isConnected()) {
			ctx.getChannel().close();
		}
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		Object message = e.getMessage();
		if (message instanceof HttpRequest) {
			session.handleRequest(ctx, (HttpRequest) message);
		}
	}

}
