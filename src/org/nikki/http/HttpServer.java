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
package org.nikki.http;

import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.nikki.http.configuration.ConfigurationNode;
import org.nikki.http.configuration.ConfigurationParser;
import org.nikki.http.content.ContentHandler;
import org.nikki.http.content.ContentManager;
import org.nikki.http.content.ErrorManager;
import org.nikki.http.content.HttpResponseException;
import org.nikki.http.module.ModuleLoader;
import org.nikki.http.net.HttpServerPipeline;
import org.nikki.http.net.HttpSession;
import org.nikki.http.util.MimeUtil;

/**
 * A basic HTTP Server implementation
 * 
 * @author Nikki
 * 
 */
public class HttpServer {

	/**
	 * The server software..
	 */
	public static final String SERVER_SOFTWARE = "JavaHttpd";

	/**
	 * The server version
	 */
	public static final String SERVER_VERSION = "0.1";

	/**
	 * The logger instance
	 */
	private static final Logger logger = Logger.getLogger(HttpServer.class
			.getName());

	/**
	 * The main entry point
	 * 
	 * @param args
	 *            The commandline args
	 */
	public static void main(String[] args) {
		HttpServer server = new HttpServer();
		try {
			server.loadConfig(new File("conf/http.conf"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		server.start();
	}

	/**
	 * The executor for networking
	 */
	private ExecutorService service = Executors.newCachedThreadPool();

	/**
	 * The configuration node
	 */
	private ConfigurationNode config;

	/**
	 * Popular index files, can be changed in config
	 */
	private String[] indexFiles = new String[] { "index.html", "index.htm" };

	/**
	 * The module loader
	 */
	private ModuleLoader moduleLoader = new ModuleLoader(this);

	/**
	 * The content manager
	 */
	private ContentManager contentManager = new ContentManager();

	/**
	 * The HTTP Error manager
	 */
	private ErrorManager errorManager = new ErrorManager(this);

	/**
	 * The document root
	 */
	private File documentRoot;

	/**
	 * Directory listing
	 */
	private boolean directoryListingEnabled = false;

	/**
	 * The server bootstrap.
	 */
	private final ServerBootstrap bootstrap = new ServerBootstrap();

	/**
	 * Construct a new HttpServer
	 */
	public HttpServer() {
		MimeUtil.loadTypes();
	}

	/**
	 * Get the server's content manager
	 * 
	 * @return The content manager
	 */
	public ContentManager getContentManager() {
		return contentManager;
	}

	/**
	 * Get the server's document root
	 * 
	 * @return The document root
	 */
	public File getDocumentRoot() {
		return documentRoot;
	}

	/**
	 * Handle a server request
	 * 
	 * @param session
	 *            The session
	 * @return The request if the server should respond by itself, or null if
	 *         the module will
	 */
	public void handleRequest(HttpSession session) {
		HttpRequest request = session.getRequest();
		// Trim off the query string, messes with our paths on Windows
		String uri = request.getUri();
		if (uri.indexOf('?') != -1) {
			uri = uri.substring(0, uri.indexOf('?'));
		}
		// Construct the file path here
		File requestFile = new File(documentRoot, uri);

		// Netty doesn't do it's own verification of URIs
		try {
			// Such a small method was overlooked and one of the testers found
			// my bash history/passwd file using it, oops...
			if (!requestFile.getCanonicalPath().startsWith(
					documentRoot.getAbsolutePath())) {
				sendHttpError(session, HttpResponseStatus.BAD_REQUEST);
				return;
			}
		} catch (IOException e) {
			// If there are any problems, we don't want to serve it, no matter
			// what security is the #1 priority
			sendHttpError(session, HttpResponseStatus.INTERNAL_SERVER_ERROR);
			return;
		}

		// Phew, now that the verification is done...
		
		// Indexes
		if (requestFile.isDirectory()) {
			boolean found = false;
			// Fix directories that don't end with /, more elegant
			if (request.getUri().charAt(request.getUri().length() - 1) != '/') {
				HttpResponse redirect = new DefaultHttpResponse(HTTP_1_1,
						HttpResponseStatus.TEMPORARY_REDIRECT);
				redirect.addHeader("Location", request.getUri() + "/");
				session.sendHttpResponse(redirect);
				return;
			}

			for (String string : indexFiles) {
				File file = new File(requestFile, string);
				if (file.exists()) {
					request.setUri(request.getUri() + string);
					found = true;
					break;
				}
			}
			if (!found) {
				if (directoryListingEnabled) {
					try {
						contentManager.getDirectoryListHandler().handleRequest(session);
					} catch (HttpResponseException e) {
						sendHttpError(session, e.getStatus());
					}
					return;
				}
			}
		}
		ContentHandler handler = contentManager.getHandlerFor(session.getRequest());
		try {
			if (handler == null) {
				throw new HttpResponseException(HttpResponseStatus.NOT_FOUND);
			}
			handler.handleRequest(session);
		} catch (HttpResponseException e) {
			sendHttpError(session, e.getStatus());
		}
	}

	/**
	 * Load the configuration
	 * 
	 * @param file
	 *            The file to load from
	 */
	public void loadConfig(File file) throws IOException {
		ConfigurationParser parser = new ConfigurationParser(file);
		this.config = parser.parse();
	}

	/**
	 * Send the appropriate error page
	 * 
	 * @param session
	 *            The session
	 * @param status
	 *            The error status
	 */
	public void sendHttpError(HttpSession session, HttpResponseStatus status) {
		HttpResponse response = errorManager.getErrorPage(status);
		if (response != null) {
			session.sendHttpResponse(response);
		} else {
			session.sendHttpResponse(new DefaultHttpResponse(
					HttpVersion.HTTP_1_1, status));
		}
	}

	/**
	 * Start the server with the port from the configuration
	 */
	public void start() {
		if (config == null) {
			throw new IllegalArgumentException("Configuration file not loaded!");
		}
		ConfigurationNode server = config.nodeFor("server");
		if (server.has("listen_port")) {
			start(new InetSocketAddress(server.getInteger("listen_port")));
		} else {
			throw new IllegalArgumentException("No port to listen on!");
		}
		if (server.has("document_root")) {
			documentRoot = new File(server.getString("document_root"));
		} else {
			documentRoot = new File("html");
		}
		if (server.has("index_file")) {
			indexFiles = server.getString("index_file").split(" ");
		}
		if (server.has("directory_listing")) {
			directoryListingEnabled = server.getString("directory_listing")
					.equals("enabled");
		}
		// Modules, loaded into a single class that can register all resources
		// needed
		ConfigurationNode modules = config.nodeFor("modules");
		if (modules.has("module")) {
			ConfigurationNode module = modules.nodeFor("module");
			for (Map.Entry<String, Object> child : module.getChildren()
					.entrySet()) {
				moduleLoader.loadModule(child.getKey(),
						(ConfigurationNode) child.getValue());
			}
		}
	}

	/**
	 * Start the server
	 */
	public void start(SocketAddress address) {
		bootstrap.setPipelineFactory(new HttpServerPipeline(this));
		bootstrap
				.setFactory(new NioServerSocketChannelFactory(service, service));

		logger.info("Binding to " + address);
		bootstrap.bind(address);
	}
}
