package org.nikki.http.fastcgi.net;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;
import org.nikki.http.fastcgi.FastCGIRequest;

/**
 * An encoder to simply call FastCGIRequest.toBuffer() on a write
 * @author Nikki
 *
 */
public class FastCGIEncoder extends OneToOneEncoder {

	@Override
	protected Object encode(ChannelHandlerContext ctx, Channel channel,
			Object object) throws Exception {
		if(object instanceof FastCGIRequest) {
			return ((FastCGIRequest) object).toBuffer();
		}
		return null;
	}
}
