package net.geertvos.theater.core.networking;

import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

/**
 * @author Geert Vos
 */
public class PartitionMessageEncoder extends OneToOneEncoder {

	private final static ObjectMapper mapper = new ObjectMapper();

	@Override
	public Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
		return mapper.writeValueAsString(msg)+"\n";
	}

}
