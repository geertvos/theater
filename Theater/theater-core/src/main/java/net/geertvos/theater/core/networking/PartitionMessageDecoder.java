package net.geertvos.theater.core.networking;

import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;

/**
 * @author Geert Vos
 */
public class PartitionMessageDecoder extends OneToOneDecoder {

	private static final ObjectMapper mapper = new ObjectMapper();
	
	@Override
	public Object decode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
		String message = (String)msg;
		PartitionMessage partitionMessage = mapper.readValue(message, PartitionMessage.class);
		return partitionMessage;
	}
	
}
