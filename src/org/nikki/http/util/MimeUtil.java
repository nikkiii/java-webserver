package org.nikki.http.util;

import java.io.File;
import java.io.IOException;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.nikki.http.configuration.ConfigurationNode;
import org.nikki.http.configuration.ConfigurationParser;

/**
 * A class to provide the server with mime types
 * 
 * @author Nikki
 * 
 */
public class MimeUtil {

	/**
	 * The logger
	 */
	private static final Logger logger = Logger.getLogger(MimeUtil.class
			.getName());

	/**
	 * The default configuration file
	 */
	private static final File MIME_TYPE_FILE = new File("conf/mime.conf");

	/**
	 * The mime types parsed from the configuration file
	 */
	private static final HashMap<String, String> mimeTypes = new HashMap<String, String>();

	/**
	 * The default java type map
	 */
	private static final FileNameMap fileNameMap = URLConnection
			.getFileNameMap();

	/**
	 * Get the mime type for the specified filename
	 * 
	 * @param fileName
	 *            The file name
	 * @return The mime type from our map, java's map or the standard mime type
	 */
	public static String getMimeType(String fileName) {
		if (fileName.contains(".")) {
			// Try our list, it is the most up to date
			String extension = fileName
					.substring(fileName.lastIndexOf('.') + 1);
			if (mimeTypes.containsKey(extension)) {
				return mimeTypes.get(extension);
			}
		}
		// Try the java default map
		String defaultMime = fileNameMap.getContentTypeFor(fileName);
		if (defaultMime != null) {
			return defaultMime;
		}
		return "application/octet-steam";
	}

	/**
	 * Load the mime types from our file
	 */
	public static void loadTypes() {
		if (!mimeTypes.isEmpty()) {
			return;
		}
		try {
			ConfigurationParser parser = new ConfigurationParser(MIME_TYPE_FILE);
			ConfigurationNode node = parser.parse();
			ConfigurationNode types = node.nodeFor("mimeTypes");
			for (Map.Entry<String, Object> entry : types.getChildren()
					.entrySet()) {
				String type = entry.getKey();
				String value = entry.getValue().toString();
				String[] split = value.split(" ");
				for (String s : split) {
					mimeTypes.put(s, type);
				}
			}
			logger.info("Loaded " + mimeTypes.size() + " extensions");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
