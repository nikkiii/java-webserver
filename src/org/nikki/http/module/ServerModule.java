package org.nikki.http.module;

import org.nikki.http.HttpServer;
import org.nikki.http.configuration.ConfigurationNode;

/**
 * A basic server module
 * 
 * @author Nikki
 *
 */
public abstract class ServerModule {
	
	/**
	 * The HttpServer instance
	 */
	protected HttpServer server;
	
	/**
	 * Called when the module is first loaded, this method should register any file extensions with the server
	 * @throws ModuleLoadingException
	 * 			Should be thrown if anything happens.
	 */
	public abstract void onLoad(ConfigurationNode node) throws ModuleLoadingException;

	/**
	 * Called when the module is unloaded, this method should cleanup any resources the module used
	 * @throws ModuleLoadingException
	 * 			Should be thrown if anything happens.
	 */
	public abstract void onUnload() throws ModuleLoadingException;
	
	/**
	 * Set the HTTP Server, useful so that we don't have to have it in the constructor
	 * @param server
	 * 			The server
	 */
	public void setServer(HttpServer server) {
		this.server = server;
	}
	
	/**
	 * The plugin should override this..
	 * @return
	 */
	public String getName() {
		return this.getClass().getName();
	}
}
