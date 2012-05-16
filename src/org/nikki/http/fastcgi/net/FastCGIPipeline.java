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
		pipeline.addLast("handler", new FastCGIChannelHandler(module));
		return pipeline;
	}
}
