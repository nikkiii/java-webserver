package org.nikki.http.net;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.nikki.http.HttpServer;

/**
 * A pipeline factory which attaches HTTP Encoders/decoders to the channel's input/output
 * 
 * @author Nikki
 *
 */
public class HttpServerPipeline implements ChannelPipelineFactory {

	/**
	 * The http server.
	 */
	private final HttpServer server;

	/**
	 * Creates a new HTTPServerPipeline.
	 */
	public HttpServerPipeline(HttpServer server) {
		this.server = server;
	}

	@Override
	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = Channels.pipeline();
		pipeline.addLast("decoder", new HttpRequestDecoder());
		pipeline.addLast("encoder", new HttpResponseEncoder());
		pipeline.addLast("handler", new HttpChannelHandler(server));
		return pipeline;
	}
}
