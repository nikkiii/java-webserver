package org.nikki.http.fastcgi;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.nikki.http.HttpSession;
import org.nikki.http.configuration.ConfigurationNode;
import org.nikki.http.fastcgi.net.FastCGIPipeline;
import org.nikki.http.module.ContentModule;
import org.nikki.http.module.ModuleLoadingException;

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
	 * The address of the FastCGI Applicatno
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

	@Override
	public void onLoad(ConfigurationNode configuration) throws ModuleLoadingException {
		bootstrap = new ClientBootstrap(
				new NioClientSocketChannelFactory(
						Executors.newCachedThreadPool(),
						Executors.newCachedThreadPool()));
		bootstrap.setPipelineFactory(new FastCGIPipeline(this));
		
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
			int children = 4;
			if(spawnConfig.has("children")) {
				children = spawnConfig.getInteger("children");
			}
			try {
				Process p = Runtime.getRuntime().exec("/bin/sh spawn-fcgi -d \""+new File("scripts").getAbsolutePath()+"\" -p "+port+" -C "+children+" "+spawnConfig.getString("bin-path"));
				BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
				while(true) {
					String line = reader.readLine();
					if(line == null)
						break;
					System.out.println("- "+line);
				}
				reader.close();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
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
					arg0.getCause().printStackTrace();
				}
			}
			
		});
	}
	
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
			//TODO cheap hack for max requests
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
		return requests.get(id);
	}
}
