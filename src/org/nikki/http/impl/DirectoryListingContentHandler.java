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

package org.nikki.http.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.nikki.http.HttpServer;
import org.nikki.http.net.HttpSession;
import org.nikki.http.util.FileUtil;
import org.nikki.http.util.Filter;

/**
 * A ContentHandler to handle directory listings
 * 
 * @author Nikki
 *
 */
public class DirectoryListingContentHandler extends ContentHandler {

	/**
	 * Load it into memory, it's not a big deal
	 */
	private String base;

	/**
	 * The file name map used for getting content types
	 */
	private FileNameMap fileNameMap = URLConnection.getFileNameMap();
	
	/**
	 * The date modified format
	 */
	private SimpleDateFormat format = new SimpleDateFormat(
			"yyyy-MMM-dd HH:mm:ss");

	/**
	 * Load our base HTML
	 */
	public DirectoryListingContentHandler() {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(
					"conf/html/dirlist.html"));
			try {
				base = "";
				String line;
				while ((line = reader.readLine()) != null) {
					base += line + "\n";
				}
			} finally {
				reader.close();
			}
		} catch (IOException e) {
			base = "Unable to load directory listing page";
		}
	}

	@Override
	public HttpResponse handleRequest(HttpSession session) {
		File directory = new File(session.getServer().getDocumentRoot(),
				session.getRequest().getUri());
        String filePath = session.getRequest().getUri();
		if (directory.exists()) {
			List<File> directories = FileUtil.listFiles(directory,
					new Filter<File>() {
						@Override
						public boolean accept(File t) {
							return t.isDirectory();
						}
					});
			Collections.sort(directories);
			StringBuilder fileList = new StringBuilder();
			for (File file : directories) {
				fileList.append("<tr>")
						.append("<td class=\"n\"><a href=\"" + filePath + "/" + file.getName()
								+ "\">" + file.getName() + "</a>/</td>")
						.append("<td class=\"m\">" + getModified(file)
								+ "</td>")
						.append("<td class=\"s\">- &nbsp;</td>")
						.append("<td class=\"t\">Directory</td>")
						.append("</tr>").append("\n");
			}
			List<File> files = FileUtil.listFiles(directory,
					new Filter<File>() {
						@Override
						public boolean accept(File t) {
							return !t.isDirectory();
						}
					});
			Collections.sort(files);
			for (File file : files) {
				fileList.append("<tr>")
						.append("<td class=\"n\"><a href=\"" + filePath + "/" + file.getName()
								+ "\">" + file.getName() + "</a></td>")
						.append("<td class=\"m\">" + getModified(file)
								+ "</td>")
						.append("<td class=\"s\">"
								+ FileUtil.humanReadableByteCount(file.length())
								+ "</td>")
						.append("<td class=\"t\">"+getMimeType(file.getName())+"</td>")
						.append("</tr>").append("\n");
			}
			String string = base.replace("{directory}", session.getRequest()
					.getUri());
			string = string.replace("{files}", fileList.toString());
			string = string.replace("{version}", HttpServer.SERVER_SOFTWARE + " " + HttpServer.SERVER_VERSION);

			HttpResponse response = new DefaultHttpResponse(
					HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
			response.setContent(ChannelBuffers.copiedBuffer(string,
					Charset.forName("UTF-8")));
			response.setHeader(HttpHeaders.Names.CONTENT_TYPE, "text/html");
			return response;
		}
		return null;
	}

	/**
	 * Format the file's modified time for the page
	 * @param file
	 * 			The file
	 * @return
	 * 			The formatted timestamp
	 */
	public String getModified(File file) {
		return format.format(new Date(file.lastModified()));
	}

	/**
	 * Get the mime type for the file
	 * @param fileName
	 * 			The file name
	 * @return
	 * 			The file type
	 */
	public String getMimeType(String fileName) {
		return fileNameMap.getContentTypeFor(fileName);
	}
}
