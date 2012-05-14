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

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.nikki.http.configuration.ConfigurationNode;
import org.nikki.http.fastcgi.net.FastCGIPipeline;
import org.nikki.http.module.ContentModule;
import org.nikki.http.module.ModuleLoadingException;
import org.nikki.http.net.HttpSession;

/**
 * A module to handle FastCGI Requests
 * @author Nikki
 *
 */
public class FastCGIModule extends ContentModule {
	
	/**
	 * The Netty client bootstrap for this module
	 */
	private ClientBootstrap bootstrap;
	
	/**
	 * The current channel
	 */
	private Channel channel;
	
	/**
	 * The address of the FastCGI Application
	 */
	private InetSocketAddress address;

	/**
	 * Stores requests by id -> HttpSession, so that requests can be sent back when they are received
	 */
	private HashMap<Integer, HttpSession> requests = new HashMap<Integer, HttpSession>();
	
	/**
	 * The current request id, I learned the hard way, thread safety is a must...
	 */
	private int requestId = 0;
	
	/**
	 * Class constructor
	 */
	public FastCGIModule() {
		bootstrap = new ClientBootstrap(
				new NioClientSocketChannelFactory(
						Executors.newCachedThreadPool(),
						Executors.newCachedThreadPool()));
		bootstrap.setPipelineFactory(new FastCGIPipeline(this));
	}

	@Override
	public void onLoad(ConfigurationNode configuration) throws ModuleLoadingException {
		String host = "127.0.0.1";
		int port = 9123;
		if(configuration.has("address")) {
			String addr = configuration.getString("address");
			if(addr.contains(":")) {
				String[] split = addr.split(":");
				host = split[0];
				port = Integer.parseInt(split[1]);
			}
		}
		address = new InetSocketAddress(host, port);
		if(configuration.has("spawn")) {
			ConfigurationNode spawnConfig = configuration.nodeFor("spawn");
			if(!spawnConfig.has("bin-path")) {
				throw new ModuleLoadingException("No binary path!");
			}
			//TODO SpawnFcgi/Windows equiv here
		}
		
		//TODO messy connection :(
		connect();
		
		FastCGIContentHandler handler = new FastCGIContentHandler(this);
		if(configuration.has("extensions")) {
			String[] split = configuration.getString("extensions").split(",");
			for(String s : split) {
				s = s.trim();
				registerExtension(s, handler);
			}
		}
	}
	
	/**
	 * Connect the FastCGI Socket
	 */
	public void connect() {
		ChannelFuture future = bootstrap.connect(address);
		future.addListener(new ChannelFutureListener() {

			@Override
			public void operationComplete(ChannelFuture arg0) throws Exception {
				if(arg0.isSuccess()) {
					channel = arg0.getChannel();
				} else {
					//TODO disable the module or something...
					arg0.getCause().printStackTrace();
				}
			}
			
		});
	}
	
	/**
	 * Called when the server unloads the module
	 */
	public void onUnload() {
		if(channel != null) {
			channel.close();
		}
	}
	
	/**
	 * Handle a request
	 * @param session
	 * 				The session which requested it
	 * @return
	 * 			Always null, since the result is sent back by the handler
	 */
	public HttpResponse handle(HttpSession session) {
		if(!channel.isConnected()) {
			//TODO cheap hack for max requests, we should make a callback for connect() so we can make it finish our request
			connect();
			throw new IllegalArgumentException("Invalid fastcgi socket!");
		}
		try {
			requestId++;
			if(requestId >= Short.MAX_VALUE) {
				requestId = 0;
			}
			
			FastCGIRequest fcgiRequest = new FastCGIRequest(session, requestId);
			
			requests.put(requestId, session);
			
			fcgiRequest.construct();
			channel.write(fcgiRequest);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Get the session for the specified id
	 * @param id
	 * 			The request id
	 * @return
	 * 			The session attached to the id
	 */
	public HttpSession getRequest(int id) {
		return requests.remove(id);
	}
}
