package net.geertvos.theater.core.networking;

import java.util.UUID;

import net.geertvos.theater.core.actor.ActorIdImpl;
import net.geertvos.theater.core.serialization.UUIDSerializer;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;

/**
 * @author Geert Vos
 */
public class SegmentMessageEncoder extends OneToOneEncoder {

	private Kryo kryo;

	public SegmentMessageEncoder() {
		kryo = new Kryo();
		kryo.register(SegmentMessage.class);
		kryo.register(ActorIdImpl.class);
		kryo.addDefaultSerializer(UUID.class, UUIDSerializer.class);
	}
	
	
	@Override
	public Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
		SegmentMessage pm = (SegmentMessage)msg;
        Output out = new Output(4096, Integer.MAX_VALUE);
        kryo.writeObject(out, pm);
        return ChannelBuffers.wrappedBuffer(out.getBuffer());
	}

}