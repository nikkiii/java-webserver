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

package org.nikki.http.fastcgi.net;

import static org.nikki.http.fastcgi.FastCGIConstants.FCGI_END_REQUEST;
import static org.nikki.http.fastcgi.FastCGIConstants.FCGI_REQUEST_COMPLETE;
import static org.nikki.http.fastcgi.FastCGIConstants.FCGI_STDERR;
import static org.nikki.http.fastcgi.FastCGIConstants.FCGI_STDOUT;

import java.util.HashMap;
import java.util.logging.Logger;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.replay.ReplayingDecoder;
import org.nikki.http.fastcgi.FastCGIResponse;

/**
 * A decoder to decode the FastCGI Response
 * 
 * @author Nikki
 * 
 */
public class FastCGIDecoder extends ReplayingDecoder<ResponseState> {

	/**
	 * It's here for now, when the type is STDERR we could redirect it to a log
	 * file..
	 */
	private static final Logger logger = Logger.getLogger(FastCGIDecoder.class
			.getName());

	/**
	 * The FCGI version, usually the same
	 */
	@SuppressWarnings("unused")
	private int version;

	/**
	 * Request type
	 */
	private int type;

	/**
	 * Request id, will be unique
	 */
	private int id;

	/**
	 * Request length
	 */
	private int contentLength;

	/**
	 * Request padding, if any
	 */
	private int paddingLength;

	/**
	 * The data from each request, store it until we have all the data, then
	 * dispatch to the handler
	 */
	private HashMap<Integer, ChannelBuffer> dataBuffers = new HashMap<Integer, ChannelBuffer>();

	/**
	 * Create a new FastCGIDecoder
	 */
	public FastCGIDecoder() {
		super(ResponseState.VERSION);
	}

	@Override
	protected Object decode(ChannelHandlerContext arg0, Channel arg1,
			ChannelBuffer buffer, ResponseState state) throws Exception {
		switch (state) {
		case VERSION:
			version = buffer.readByte();
			checkpoint(ResponseState.HEADER);
		case HEADER:
			// The types are unsigned in the FCGI docs
			type = buffer.readByte() & 0xFF;
			id = buffer.readShort() & 0xFFFF;
			contentLength = buffer.readShort() & 0xFFFF;
			paddingLength = buffer.readByte() & 0xFFFF;
			buffer.readByte(); // Reserved byte...
			logger.finest("Read request header : type=" + type + ",id=" + id
					+ ",contentLength=" + contentLength + ",paddingLength="
					+ paddingLength);
			checkpoint(ResponseState.CONTENT);
		case CONTENT:
			switch (type) {
			case FCGI_END_REQUEST:
				// Statuses, don't know what the correct use is, but
				// FCGI_REQUEST_COMPLETE is one, and there's another status
				// code...
				int appStatus = buffer.readInt();
				int protocolStatus = buffer.readByte();
				// Reserved bytes
				buffer.skipBytes(3);
				if (appStatus != 0 || protocolStatus != FCGI_REQUEST_COMPLETE) {
					// Uh oh, when we run a pool of connections we should close
					// this one here...
					logger.warning("Protocol status " + protocolStatus);
				} else if (protocolStatus == FCGI_REQUEST_COMPLETE) {
					// Reset the version
					checkpoint(ResponseState.VERSION);
					// Merged from ChannelBuffer buffer ... dataBuffers.remove
					// to this, remove() returns the object
					return new FastCGIResponse(id, dataBuffers.remove(id));
				}
				checkpoint(ResponseState.VERSION);
				break;
			case FCGI_STDOUT:
				if (contentLength > 0) {
					// Have to store it, just in case we get a new request
					if (!dataBuffers.containsKey(id)) {
						dataBuffers.put(id, ChannelBuffers.dynamicBuffer());
					}
					logger.finest("Got " + contentLength
							+ " bytes for request " + id);
					// Write the bytes to the buffer
					dataBuffers.get(id).writeBytes(
							buffer.readBytes(contentLength));
					// Checkpoint!
					checkpoint(ResponseState.VERSION);
				}
				break;
			// solaroperator pointed out this never contains HTML, I forgot a
			// checkpoint so it stayed STDERR even with 0 bytes
			case FCGI_STDERR:
				byte[] err = new byte[contentLength];
				buffer.readBytes(err);

				// TODO error logs
				logger.warning(new String(err));

				// In the original version where this was commented out, this
				// was never here, causing all data to go into STDERR
				checkpoint(ResponseState.VERSION);
				break;
			default:
				logger.warning("Unknown type " + type);
				checkpoint(ResponseState.VERSION);
				break;
			}
			if (paddingLength > 0) {
				logger.finest("Skipping " + paddingLength + " bytes of padding");
				buffer.skipBytes(paddingLength);
			}
			break;
		}
		return null;
	}
}
