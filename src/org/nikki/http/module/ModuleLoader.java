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

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.nikki.http.HttpServer;
import org.nikki.http.configuration.ConfigurationNode;

/**
 * A simple module loader
 * 
 * @author Nikki
 * 
 */
public class ModuleLoader {

	private static final Logger logger = Logger.getLogger(ModuleLoader.class
			.getName());

	/**
	 * A list of the loaded modules
	 */
	private LinkedList<ServerModule> loadedModules = new LinkedList<ServerModule>();

	/**
	 * The HTTP Server instance
	 */
	private HttpServer server;

	/**
	 * Construct a new module loader
	 * 
	 * @param server
	 *            The HTTP Server instance
	 */
	public ModuleLoader(HttpServer server) {
		this.server = server;
	}

	/**
	 * Load a server module, the configuration can specify the source to be a
	 * jar file in the module directory
	 * 
	 * @param className
	 *            The module main class
	 * @param configuration
	 *            The module configuration file
	 */
	public void loadModule(String className, ConfigurationNode configuration) {
		try {
			ClassLoader loader = ModuleLoader.class.getClassLoader();
			if (configuration.has("source")) {
				loader = new URLClassLoader(new URL[] { new File(
						configuration.getString("source")).toURI().toURL() });
			}
			Class<?> cl = loader.loadClass(className);
			ServerModule module = (ServerModule) cl.newInstance();
			module.setServer(server);
			module.onLoad(configuration);
			// Register the module
			register(module);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Unable to load module : " + className, e);
		}
	}

	/**
	 * Register a module
	 * 
	 * @param module
	 *            The module to register
	 */
	public void register(ServerModule module) {
		try {
			logger.info("Loaded module : " + module.getName());
			synchronized (loadedModules) {
				loadedModules.add(module);
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error while registering module : "
					+ module.getName() + "!", e);
		}
	}

	/**
	 * Unregister a module
	 * 
	 * @param module
	 *            The module to unregister
	 */
	public void unregister(ServerModule module) {
		try {
			// Call the unload method of the module, which should be used to
			// unregister listeners!
			module.onUnload();
			logger.fine("Unloaded module : " + module.getName());
			synchronized (loadedModules) {
				loadedModules.remove(module);
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error while unregistering module : "
					+ module.getName() + "!", e);
		}
	}
}
