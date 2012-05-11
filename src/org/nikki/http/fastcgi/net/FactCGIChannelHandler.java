package org.nikki.http.fastcgi.net;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.nikki.http.HttpSession;
import org.nikki.http.fastcgi.FastCGIModule;
import org.nikki.http.fastcgi.FastCGIResponse;
import org.nikki.http.fastcgi.FastCGIUtil;

/**
 * A channel handler for FastCGI Requests
 * 
 * @author Nikki
 *
 */
public class FactCGIChannelHandler extends SimpleChannelUpstreamHandler {

	/**
	 * The fastcgi module instance
	 */
	private FastCGIModule module;

	/**
	 * Creates a new HTTPChannelHandler.
	 * @param server
	 */
	public FactCGIChannelHandler(FastCGIModule module) {
		this.module = module;
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		Object message = e.getMessage();
		if(message instanceof FastCGIResponse) {
			FastCGIResponse response = (FastCGIResponse) message;
			
			ChannelBuffer buffer = response.getData();
			
			HttpResponse res = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
			
			//Parse the headers off the buffer, and copy them into the response
			FastCGIUtil.parseHeaders(res, buffer);
			
			//Set the content to the remaining bytes
			res.setContent(buffer.readBytes(buffer.readableBytes()));
			
			//Get the current session for this response id, and send the response
			HttpSession session = module.getRequest(response.getId());
			if(session != null) {
				session.sendHttpResponse(res);
			}
		}
	}

}
