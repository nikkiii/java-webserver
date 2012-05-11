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
 * @author Nikki
 *
 */
public class FastCGIDecoder extends ReplayingDecoder<ResponseState> {
	
	/**
	 * It's here for now, when the type is STDERR we could redirect it to a log file..
	 */
	private static final Logger logger = Logger.getLogger(FastCGIDecoder.class.getName());

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
	private int length;
	
	/**
	 * Request padding, if any
	 */
	private int padding;
	
	/**
	 * The data from each request, store it until we have all the data, then dispatch to the handler
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
			ChannelBuffer buffer, ResponseState state)
			throws Exception {
		switch(state) {
		case VERSION:
			//If there's padding on the last request we need to skip it
			if(padding > 0) {
				buffer.skipBytes(padding);
			}
			version = buffer.readByte();
			checkpoint(ResponseState.HEADER);
		case HEADER:
			type = buffer.readByte();
			id = buffer.readShort();
			length = buffer.readShort();
			padding = buffer.readByte();
			buffer.readByte(); //Reserved byte...
			checkpoint(ResponseState.CONTENT);
		case CONTENT:
			switch(type) {
			case FCGI_END_REQUEST:
				//Statuses, don't know what the correct use is, but FCGI_REQUEST_COMPLETE is one, and there's another status code...
				int appStatus = buffer.readInt();
				int pStatus = buffer.readByte();
				//Reserved bytes
				buffer.skipBytes(3);
				if(appStatus != 0 || pStatus != FCGI_REQUEST_COMPLETE) {
					//Uh oh, when we run a pool of connections we should close this one here...
				} else if(pStatus == FCGI_REQUEST_COMPLETE) {
					//Get the request buffer and return a response (We need to store it in case of multiple requests)
					ChannelBuffer dataBuffer = dataBuffers.get(id);
					dataBuffers.remove(id);
					checkpoint(ResponseState.VERSION);
					return new FastCGIResponse(id, dataBuffer);
				}
				checkpoint(ResponseState.VERSION);
				break;
			case FCGI_STDERR:
			case FCGI_STDOUT:
				if(length == 0) {
					if(padding > 0)
						buffer.skipBytes(padding);
				} else {
					//Have to store it, just in case we get a new request
					if(!dataBuffers.containsKey(id)) {
						dataBuffers.put(id, ChannelBuffers.dynamicBuffer());
					}
					logger.finest("Got "+length+" bytes for request "+id);
					//Messy, the handler needs to be redone
					dataBuffers.get(id).writeBytes(buffer.readBytes(length));
					checkpoint(ResponseState.VERSION);
				}
				break;
			//TODO STDERR sometimes contains HTML...
			/*case FCGI_STDERR:
				byte[] err = new byte[length];
				buffer.readBytes(err);
				
				logger.warning(new String(err));
				
				if(padding > 0)
					buffer.skipBytes(padding);
				break;*/
			default:
				logger.warning("Unknown type "+type);
				checkpoint(ResponseState.VERSION);
				break;
			}
			break;
		}
		return null;
	}
}
