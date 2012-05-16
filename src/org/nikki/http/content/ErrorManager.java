package org.nikki.http.content;

import java.io.File;
import java.io.IOException;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.util.CharsetUtil;
import org.nikki.http.HttpServer;
import org.nikki.http.util.FileUtil;

/**
 * A manager for the error page files
 * 
 * @author Nikki
 *
 */
public class ErrorManager {
	
	/**
	 * The HTTP Server
	 */
	private HttpServer server;

	public ErrorManager(HttpServer server) {
		this.server = server;
	}

	public HttpResponse getErrorPage(HttpResponseStatus status) {
		HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1,
				status);
		File file = new File(server.getDocumentRoot(), "error/"
				+ status.getCode() + ".html");
		if (file.exists()) {
			try {
				response.setContent(FileUtil.readFile(file));
			} catch (IOException e) {
				response.setContent(ChannelBuffers.copiedBuffer(
						status.toString(), CharsetUtil.UTF_8));
			}
		} else {
			response.setContent(ChannelBuffers.copiedBuffer(response
					.getStatus().toString(), CharsetUtil.UTF_8));
		}
		HttpHeaders.setContentLength(response, response.getContent()
				.readableBytes());
		return response;
	}
}
