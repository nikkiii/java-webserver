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

import java.io.File;
import java.io.IOException;

import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.nikki.http.net.HttpSession;
import org.nikki.http.util.FileUtil;

/**
 * The default handler, which serves files from the filesystem directly
 * @author Nikki
 *
 */
public class StaticFileContentHandler extends ContentHandler {

	@Override
	public HttpResponse handleRequest(HttpSession session) {
		String uri = session.getRequest().getUri();
		File file = new File(session.getServer().getDocumentRoot(), uri);
		if(file.exists() && !file.isDirectory()) {
			HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
			try {
				response.setContent(FileUtil.readFile(file));
			} catch(IOException e) {
				response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
			}
			return response;
		}
		return null;
	}
}
