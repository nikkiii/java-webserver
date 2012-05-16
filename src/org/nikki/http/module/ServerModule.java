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
	 * The plugin should override this..
	 * 
	 * @return
	 */
	public String getName() {
		return this.getClass().getName();
	}

	/**
	 * Called when the module is first loaded, this method should register any
	 * file extensions with the server
	 * 
	 * @throws ModuleLoadingException
	 *             Should be thrown if anything happens.
	 */
	public abstract void onLoad(ConfigurationNode node)
			throws ModuleLoadingException;

	/**
	 * Called when the module is unloaded, this method should cleanup any
	 * resources the module used
	 * 
	 * @throws ModuleLoadingException
	 *             Should be thrown if anything happens.
	 */
	public abstract void onUnload() throws ModuleLoadingException;

	/**
	 * Set the HTTP Server, useful so that we don't have to have it in the
	 * constructor
	 * 
	 * @param server
	 *            The server
	 */
	public void setServer(HttpServer server) {
		this.server = server;
	}
}
