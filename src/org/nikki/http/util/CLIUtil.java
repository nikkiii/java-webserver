package org.nikki.http.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Utilities for parsing arguments passed on program execution
 * 
 * @author Nikki
 *
 */
public class CLIUtil {

	/**
	 * Parse commandline arguments
	 * 
	 * @param args
	 *            The arg array from the main method, or manual args
	 * @return The map containing the args
	 */
	public static CLIArguments parseArguments(String[] args) {
		HashMap<String, Object> arguments = new HashMap<String, Object>();
		for (int i = 0; i < args.length; i++) {
			String s = args[i];
			if (s.startsWith("--")) {
				//It's most likely a --key=value format
				s = s.substring(2);
				//Get the index of the '=' sign
				int eqIdx = s.indexOf('=');
				//Set the key
				String key = s.substring(0, eqIdx);
				//If there's no '=', we'll set it to 'true'
				Object value = true;
				if(eqIdx != -1) {
					value = s.substring(eqIdx + 1);
				}
				//Finally, add it to the map
				arguments.put(key, value);
			} else if (s.startsWith("-")) {
				s = s.substring(1);
				//It's most likely a -key value or just a 'true' flag, we can check the next index
				Object value = true;
				//Check if we have another value
				if(args.length > i+1) {
					String val = args[i+1];
					//Make sure it doesn't start with - or --, this is usually another key
					if(!val.startsWith("-") && !val.startsWith("--")) {
						value = val;
						i++;
					}
				}
				//Finally, add it to the map
				arguments.put(s, value);
			} else {
				//Fallback, check if it has an '=' in it
				int eqIdx = s.indexOf('=');
				//If it does, get the key, if not, the key is the string itself
				String key = s.substring(0, eqIdx != -1 ? eqIdx : s.length());
				//Get the value if it has one
				Object value = true;
				if (eqIdx != -1) {
					value = s.substring(eqIdx + 1);
				}
				//Finally, add it to the map
				arguments.put(key, value);
			}
		}
		return new CLIArguments(arguments);
	}
	
	/**
	 * A wrapper to add easy use of multiple keys.
	 * Example:
	 * 	--bla=bla could be -bla bla
	 * Usage:
	 * 	CLIArguments args = CLIUtils.parseArguments(argv);
	 *  if args.hasAny("port", "p")...
	 *  int port = args.getInteger("port", "p");
	 * 
	 * @author Nikki
	 *
	 */
	public static class CLIArguments {
		
		/**
		 * The map containing our arguments
		 */
		private Map<String, Object> arguments = new HashMap<String, Object>();
		
		/**
		 * Construct a new wrapper for the arguments
		 * @param arguments
		 * 			The argument map
		 */
		public CLIArguments(Map<String, Object> arguments) {
			this.arguments = arguments;
		}
		
		/**
		 * Check if the arguments contain a certain key
		 * @param key
		 * 			The key
		 * @return
		 * 			True, if the key is in the map
		 */
		public boolean has(String key) {
			return arguments.containsKey(key);
		}
		
		/**
		 * Check if the map contains any of the specified keys
		 * @param keys
		 * 			The list of keys to check for
		 * @return
		 * 			True, if the key is in the map
		 */
		public boolean hasAny(String... keys) {
			for(String s : keys) {
				if(arguments.containsKey(s)) {
					return true;
				}
			}
			return false;
		}
		
		/**
		 * Get a Boolean value from the map (True/False)
		 * @param keys
		 * 			The key/keys to check with
		 * @return
		 * 			The boolean, if found
		 */
		public boolean getBoolean(String... keys) {
			for(String s : keys) {
				if(arguments.containsKey(s)) {
					return Boolean.parseBoolean(getString(keys));
				}
			}
			throw new IllegalArgumentException("Invalid key! Use has(key) or hasAny(keys...) to check first!");
		}
		
		/**
		 * Get a String value from the map
		 * @param keys
		 * 			The key/keys to check with
		 * @return
		 * 			The String, if found
		 */
		public String getString(String... keys) {
			for(String s : keys) {
				if(arguments.containsKey(s)) {
					return arguments.get(s).toString();
				}
			}
			throw new IllegalArgumentException("Invalid key! Use has(key) or hasAny(keys...) to check first!");
		}
		
		/**
		 * Get an Integer value from the map (Integer.MIN_VALUE -> Integer.MAX_VALUE)
		 * @param keys
		 * 			The key/keys to check with
		 * @return
		 * 			The Integer, if found
		 */
		public int getInteger(String... keys) {
			for(String s : keys) {
				if(arguments.containsKey(s)) {
					return Integer.parseInt(arguments.get(s).toString());
				}
			}
			throw new IllegalArgumentException("Invalid key! Use has(key) or hasAny(keys...) to check first!");
		}
	}
}
