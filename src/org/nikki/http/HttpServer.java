package org.nikki.http;

import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.nikki.http.configuration.ConfigurationNode;
import org.nikki.http.configuration.ConfigurationParser;
import org.nikki.http.impl.ContentHandler;
import org.nikki.http.impl.ContentManager;
import org.nikki.http.module.ModuleLoader;
import org.nikki.http.net.HttpServerPipeline;

/**
 * A basic HTTP Server implementation
 * 
 * @author Nikki
 *
 */
public class HttpServer {
	
	/**
	 * The logger instance
	 */
	private static final Logger logger = Logger.getLogger(HttpServer.class.getName());
	
	/**
	 * The executor for networking
	 */
	private ExecutorService service = Executors.newCachedThreadPool();
	
	/**
	 * The configuration node
	 */
	private ConfigurationNode config;
	
	/**
	 * The module loader
	 */
	private ModuleLoader moduleLoader = new ModuleLoader(this);
	
	/**
	 * The content manager
	 */
	private ContentManager contentManager = new ContentManager();
	
	/**
	 * The document root
	 */
	private File documentRoot;
	
	/**
	 * The server bootstrap.
	 */
	private final ServerBootstrap bootstrap = new ServerBootstrap();
	
	/**
	 * Construct a new HttpServer
	 */
	public HttpServer() {
	}
	
	/**
	 * Load the configuration
	 * @param file
	 * 			The file to load from
	 */
	public void loadConfig(File file) throws IOException {
		ConfigurationParser parser = new ConfigurationParser(file);
		this.config = parser.parse();
	}
	
	/**
	 * Start the server with the port from the configuration
	 */
	public void start() {
		if(config == null) {
			throw new IllegalArgumentException("Configuration file not loaded!");
		}
		ConfigurationNode server = config.nodeFor("server");
		if(server.has("listen_port")) {
			start(new InetSocketAddress(server.getInteger("listen_port")));
		} else {
			throw new IllegalArgumentException("No port to listen on!");
		}
		if(server.has("document_root")) {
			documentRoot = new File(server.getString("document_root"));
		} else {
			documentRoot = new File("html");
		}
		// Modules, loaded into a single class that can register all resources needed
		ConfigurationNode modules = config.nodeFor("modules");
		if(modules.has("module")) {
			ConfigurationNode module = modules.nodeFor("module");
			for(Map.Entry<String, Object> child : module.getChildren().entrySet()) {
				moduleLoader.loadModule(child.getKey(), (ConfigurationNode) child.getValue());
			}
		}
	}

	/**
	 * Start the server
	 */
	public void start(SocketAddress address) {
		bootstrap.setPipelineFactory(new HttpServerPipeline(this));
		bootstrap.setFactory(new NioServerSocketChannelFactory(service, service));
		
		logger.info("Binding to "+address);
		bootstrap.bind(address);
	}
	
	/**
	 * Get the server's document root
	 * @return
	 * 		The document root
	 */
	public File getDocumentRoot() {
		return documentRoot;
	}
	
	/**
	 * Get the server's content manager
	 * @return
	 * 		The content manager
	 */
	public ContentManager getContentManager() {
		return contentManager;
	}
	
	
	/**
	 * Handle a server request
	 * @param session
	 * 			The session
	 * @return
	 * 			The request if the server should respond by itself, or null if the module will
	 */
	public HttpResponse handleRequest(HttpSession session) {
		ContentHandler handler = contentManager.getHandlerFor(session.getRequest());
		
		HttpResponse response = handler.handleRequest(session);
		if(response != null || handler.isAsync()) {
			return response;
		}
		//TODO 404 page
		response = new DefaultHttpResponse(HTTP_1_1, HttpResponseStatus.NOT_FOUND);
		response.setContent(ChannelBuffers.copiedBuffer(response.getStatus().toString() + session.getRequest().getUri(), Charset.forName("UTF-8")));
		return response;
	}
	
	public static void main(String[] args) {
		HttpServer server = new HttpServer();
		try {
			server.loadConfig(new File("conf/http.conf"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		server.start();
	}
}
