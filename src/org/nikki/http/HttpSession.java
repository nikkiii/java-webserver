package org.nikki.http;

import static org.jboss.netty.handler.codec.http.HttpHeaders.setContentLength;

import java.io.File;
import java.io.IOException;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.util.CharsetUtil;
import org.nikki.http.impl.StaticFileContentHandler;

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
	 * Handle the request
	 * @param ctx
	 * 			The ChannelHandlerContext of the connectino
	 * @param request
	 * 			The HTTP Request
	 */
	public void handleRequest(ChannelHandlerContext ctx, HttpRequest request) {
		this.request = request;
		this.channel = ctx.getChannel();
		
		HttpResponse response = server.handleRequest(this);
		if(response != null) {
			sendHttpResponse(response);
		}
	}
	
	/**
	 * Sends the http response.
	 * @param ctx The channel handler context.
	 * @param req The http request.
	 * @param res The http response.
	 */
	public void sendHttpResponse(HttpResponse res) {
		if(res == null) {
			System.out.println("Response null!");
			return;
		}
		if (res.getStatus().getCode() != 200) {
			File file = new File(server.getDocumentRoot(), "error/"+res.getStatus().getCode()+".html");
			if(file.exists()) {
				try {
					res.setContent(StaticFileContentHandler.readFile(file));
				} catch(IOException e) {
					res.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
				}
			} else {
				res.setContent(ChannelBuffers.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8));
				setContentLength(res, res.getContent().readableBytes());
			}
		}
		channel.write(res).addListener(ChannelFutureListener.CLOSE);
	}
	
	/**
	 * Get the channel
	 * @return
	 * 		The session's channel
	 */
	public Channel getChannel() {
		return channel;
	}
	
	/**
	 * Get the server this session belongs to
	 * @return
	 * 		The server
	 */
	public HttpServer getServer() {
		return server;
	}

	/**
	 * Get the HTTP Request
	 * @return
	 * 		The request
	 */
	public HttpRequest getRequest() {
		return request;
	}
}
