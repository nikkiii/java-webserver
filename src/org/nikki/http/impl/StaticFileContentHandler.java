package org.nikki.http.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.nikki.http.HttpSession;

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
		if(file.exists()) {
			HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
			try {
				response.setContent(readFile(file));
			} catch(IOException e) {
				response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
			}
			return response;
		}
		return null;
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
