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

package org.nikki.http.content;

import static org.jboss.netty.handler.codec.http.HttpHeaders.setContentLength;

import java.io.File;
import java.io.RandomAccessFile;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.DefaultFileRegion;
import org.jboss.netty.channel.FileRegion;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.nikki.http.net.HttpSession;
import org.nikki.http.util.MimeUtil;

/**
 * The default handler, which serves files from the filesystem directly This
 * also sets cache control, meaning faster requests
 * 
 * @author Nikki
 * 
 */
public class StaticFileContentHandler implements ContentHandler {

	/**
	 * The date format used by common browsers
	 */
	public static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
	
	/**
	 * The timezone
	 */
	public static final String HTTP_DATE_GMT_TIMEZONE = "GMT";
	
	/**
	 * How long the cached file should last for (before being checked)
	 */
	public static final int HTTP_CACHE_SECONDS = 60;

	@Override
	public void handleRequest(HttpSession session) throws HttpResponseException {
		HttpRequest request = session.getRequest();
		//Query string messes with our file paths
		String uri = request.getUri();
		if (uri.indexOf('?') != -1) {
			uri = uri.substring(0, uri.indexOf('?'));
		}
		File file = new File(session.getServer().getDocumentRoot(), uri);
		if (file.exists() && !file.isDirectory()) {
			Channel channel = session.getChannel();

			// Cache Validation, sendHttpResponse might close it, but that's ok
			// since we don't send data.
			String ifModifiedSince = request
					.getHeader(HttpHeaders.Names.IF_MODIFIED_SINCE);
			if (ifModifiedSince != null && !ifModifiedSince.equals("")) {
				try {
					SimpleDateFormat dateFormatter = new SimpleDateFormat(
							HTTP_DATE_FORMAT, Locale.US);
					Date ifModifiedSinceDate = dateFormatter
							.parse(ifModifiedSince);

					// Only compare up to the second because the datetime format
					// we send to the client does not have milliseconds
					long ifModifiedSinceDateSeconds = ifModifiedSinceDate
							.getTime() / 1000;
					long fileLastModifiedSeconds = file.lastModified() / 1000;
					if (ifModifiedSinceDateSeconds == fileLastModifiedSeconds) {
						HttpResponse response = new DefaultHttpResponse(
								HttpVersion.HTTP_1_1,
								HttpResponseStatus.NOT_MODIFIED);
						setDateHeader(response);

						session.sendHttpResponse(response);
						return;
					}
				} catch (ParseException e) {
					// Ignore it and just send
				}
			}

			try {
				// We want any exceptions we have thrown before we get into it.
				RandomAccessFile raf = new RandomAccessFile(file, "r");

				long fileLength = file.length();

				HttpResponse response = new DefaultHttpResponse(
						HttpVersion.HTTP_1_1, HttpResponseStatus.OK);

				setContentLength(response, fileLength);
				setDateAndCacheHeaders(response, file);
				response.setHeader(HttpHeaders.Names.CONTENT_TYPE, MimeUtil.getMimeType(file.getName()));

				// Now is when the weird part starts... using the system, the
				// handler -should- return the response, but the response
				// doesn't have the data... and it won't.
				session.sendHttpResponse(response, fileLength == 0);
				
				if (fileLength == 0) {
					return;
				}
				// Write the file
				final FileRegion region = new DefaultFileRegion(
						raf.getChannel(), 0, fileLength);
				ChannelFuture writeFuture = channel.write(region);
				writeFuture.addListener(new ChannelFutureListener() {
					@Override
					public void operationComplete(ChannelFuture future) {
						region.releaseExternalResources();
					}
				});
				writeFuture.addListener(ChannelFutureListener.CLOSE);
			} catch (Exception e) {
				throw new HttpResponseException(
						HttpResponseStatus.INTERNAL_SERVER_ERROR);
			}
		} else {
			throw new HttpResponseException(HttpResponseStatus.NOT_FOUND);
		}
	}

	/**
	 * Sets the Date and Cache headers for the HTTP Response
	 * 
	 * @param response
	 *            HTTP response
	 * @param fileToCache
	 *            file to extract content type
	 */
	private void setDateAndCacheHeaders(HttpResponse response, File fileToCache) {
		SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT,
				Locale.US);
		dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

		// Date header
		Calendar time = new GregorianCalendar();
		response.setHeader(HttpHeaders.Names.DATE,
				dateFormatter.format(time.getTime()));

		// Add cache headers
		time.add(Calendar.SECOND, HTTP_CACHE_SECONDS);
		response.setHeader(HttpHeaders.Names.EXPIRES,
				dateFormatter.format(time.getTime()));
		response.setHeader(HttpHeaders.Names.CACHE_CONTROL, "private, max-age="
				+ HTTP_CACHE_SECONDS);
		response.setHeader(HttpHeaders.Names.LAST_MODIFIED,
				dateFormatter.format(new Date(fileToCache.lastModified())));
	}

	/**
	 * Sets the Date header for the HTTP response
	 * 
	 * @param response
	 *            HTTP response
	 */
	private void setDateHeader(HttpResponse response) {
		SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT,
				Locale.US);
		dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

		Calendar time = new GregorianCalendar();
		response.setHeader(HttpHeaders.Names.DATE,
				dateFormatter.format(time.getTime()));
	}
}
