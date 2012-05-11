package org.nikki.http.fastcgi.net;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.nikki.http.fastcgi.FastCGIModule;

/**
 * A pipeline factory to attach to a FastCGI Connection
 * 
 * @author Nikki
 *
 */
public class FastCGIPipeline implements ChannelPipelineFactory {


	/**
	 * The FastCGI Module instance
	 */
	private FastCGIModule module;

	/**
	 * Creates a new HTTPServerPipeline.
	 */
	public FastCGIPipeline(FastCGIModule module) {
		this.module = module;
	}

	@Override
	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = Channels.pipeline();
		pipeline.addLast("decoder", new FastCGIDecoder());
		pipeline.addLast("encoder", new FastCGIEncoder());
		pipeline.addLast("handler", new FactCGIChannelHandler(module));
		return pipeline;
	}
}
