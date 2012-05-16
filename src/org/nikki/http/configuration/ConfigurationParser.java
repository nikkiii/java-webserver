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

package org.nikki.http.configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class to parse a configuration file, based on the perl configuration system
 * Azusa::Configuration
 * 
 * @author Nikki
 * @author solar
 * 
 */
public class ConfigurationParser {

	/**
	 * The pattern to match scalar lines
	 */
	private static final Pattern scalarPattern = Pattern
			.compile("\\s*(.*?)\\s*\"((?:\\\"|[^\"])+?)\"(;|)\\s*(?:\\#.*)?$");

	/**
	 * The pattern to match arrays..
	 */
	private static final Pattern arrayPattern = Pattern
			.compile("\\s*(.*?)\\s*\\s*\\{\\s*(?:\\#.*)?$");

	/**
	 * The pattern to match nested arrays
	 */
	private static final Pattern nestedArrayPattern = Pattern
			.compile("\\s*(.*?)\\s*\"((?:\\\"|[^\"])+?)\"\\s*\\(\\s*(?:\\#.*)?$");

	/**
	 * The pattern to match nested hashes
	 */
	private static final Pattern nestedHashPattern = Pattern
			.compile("\\s*(.*?)\\s*\"((?:\\\\\"|[^\"])+?)\"\\s*\\{\\s*(?:\\#.*)?$");

	/**
	 * The patter to find the end of a nested array
	 */
	private static final Pattern nestedEndPattern = Pattern
			.compile("^\\s*(}|\\))\\s*");

	/**
	 * The file
	 */
	private File file;

	/**
	 * The reader object which the configuration is parsed from
	 */
	private BufferedReader reader;

	/**
	 * Create a new configuration parser
	 * 
	 * @param file
	 *            The file to parse, also used for includes
	 * @throws IOException
	 *             If an error occurred
	 */
	public ConfigurationParser(File file) throws IOException {
		this.file = file;
		this.reader = new BufferedReader(new FileReader(file));
	}

	/**
	 * Used to close the reader in case it isn't used via parse()
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException {
		reader.close();
	}

	/**
	 * Parse the configuration from the specified file
	 * 
	 * @return The configuration
	 * @throws IOException
	 */
	public ConfigurationNode parse() throws IOException {
		ConfigurationNode node = new ConfigurationNode();
		try {
			parse(node);
		} finally {
			reader.close();
		}
		return node;
	}

	/**
	 * Parse a block of data, reading line per line from the reader.
	 */
	public void parse(ConfigurationNode node) throws IOException {
		String line = reader.readLine();
		if (line == null) {
			return;
		}
		line = line.trim();

		if (!line.startsWith("#") && line.length() != 0) {
			// Scalar match
			Matcher scalar = scalarPattern.matcher(line);
			Matcher array = arrayPattern.matcher(line);
			Matcher nestedArrayBlock = nestedArrayPattern.matcher(line);
			Matcher nestedHashBlock = nestedHashPattern.matcher(line);
			if (scalar.find()) {
				String key = scalar.group(1), value = scalar.group(2);
				if (key.equals("include_file")) {
					ConfigurationParser parser = new ConfigurationParser(
							new File(file.getParentFile(), value));
					parser.parse(node);
					parser.close();
				} else {
					node.set(key, value);
				}
			} else if (nestedArrayBlock.find()) {
				String name = nestedArrayBlock.group(1);
				String key = nestedArrayBlock.group(2);
				key = key.replaceAll("\\\"", "\"");
				if (!node.has(name)) {
					node.set(name, new ConfigurationNode());
				}
				parse(node.nodeFor(name));
			} else if (nestedHashBlock.find()) {
				String name = nestedHashBlock.group(1);
				String key = nestedHashBlock.group(2);
				ConfigurationNode sub = node.has(name) ? node.nodeFor(name)
						: new ConfigurationNode();
				if (!node.has(name)) {
					node.set(name, sub);
				}
				if (!sub.has(key)) {
					sub.set(key, new ConfigurationNode());
				}
				parse(sub.nodeFor(key));
			} else if (array.find()) {
				ConfigurationNode newNode = new ConfigurationNode();
				node.set(array.group(1), newNode);
				parse(newNode);
			}
			Matcher nestedEnd = nestedEndPattern.matcher(line);
			if (nestedEnd.find()) {
				return;
			}
		}
		parse(node);
	}
}