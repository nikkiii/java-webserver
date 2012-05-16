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

package org.nikki.http.scgi;

import org.nikki.http.configuration.ConfigurationNode;
import org.nikki.http.module.ContentModule;
import org.nikki.http.module.ModuleLoadingException;

/**
 * A module for SCGI Interaction, will be finished eventually
 * 
 * @author Nikki
 * 
 */
public class SCGIModule extends ContentModule {

	@Override
	public void onLoad(ConfigurationNode node) throws ModuleLoadingException {
		// TODO SCGI Loading

	}

	@Override
	public void onUnload() throws ModuleLoadingException {
		// TODO SCGI Unloading
	}

}
