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

package org.nikki.http.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

/**
 * A class for dealing with File operations
 * 
 * @author Nikki
 *
 */
public class FileUtil {

	/**
	 * List the files in a directory with the specified filter
	 * @param directory
	 * 			The directory
	 * @param filter
	 * 			The filter to check files against
	 * @return
	 * 			The file list
	 */
	public static List<File> listFiles(File directory, Filter<File> filter) {
		List<File> files = new LinkedList<File>();
		for(File file : directory.listFiles()) {
			if(filter.accept(file)) {
				files.add(file);
			}
		}
		return files;
	}

	/**
	 * Get all files in a directory
	 * @param directory
	 * 			The directory
	 * @return
	 * 			The files
	 */
	public static List<File> listFiles(File directory) {
		return listFiles(directory, new Filter<File>() {

			@Override
			public boolean accept(File t) {
				return true;
			}
		});
	}

	/**
	 * Convert bytes to a human readable format
	 * @param bytes
	 * 			The number to convert
	 * @return
	 * 			The formatted string
	 */
	public static String humanReadableByteCount(long bytes) {
	    int unit = 1024;
	    if (bytes < unit) return bytes + " B";
	    int exp = (int) (Math.log(bytes) / Math.log(unit));
	    String pre = "KMGTPE".charAt(exp-1) + "";
	    return String.format("%.1f %s", bytes / Math.pow(unit, exp), pre);
	}
	
	/**
	 * Read the specified file into a ChannelBuffer objecdt
	 * 
	 * @param file
	 * 			The file to read
	 * @return
	 * 			The buffer containing the file contents
	 * @throws IOException
	 * 			If an error occurred while reading
	 */
	public static ChannelBuffer readFile(File file) throws IOException {
		ChannelBuffer out = ChannelBuffers.dynamicBuffer();
		FileInputStream input = new FileInputStream(file);
		try {
			byte[] buffer = new byte[1024];
			int read;
			while((read = input.read(buffer, 0, buffer.length)) != -1) {
				out.writeBytes(buffer, 0, read);
			}
		} finally {
			input.close();
		}
		return out;
	}
}
