package net.geertvos.theater.core.networking.netty;

import java.util.UUID;

import net.geertvos.theater.core.actor.ActorHandleImpl;
import net.geertvos.theater.core.networking.SegmentMessage;
import net.geertvos.theater.core.serialization.UUIDSerializer;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferInputStream;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;

/**
 * @author Geert Vos
 */
public class SegmentMessageDecoder extends OneToOneDecoder {

	private final Kryo kryo;
	
	public SegmentMessageDecoder() {
		kryo = new Kryo();
		kryo.register(SegmentMessage.class);
		kryo.register(ActorHandleImpl.class);
		kryo.addDefaultSerializer(UUID.class, UUIDSerializer.class);
	}
	
	@Override
	public Object decode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
		ChannelBuffer buffer = (ChannelBuffer)msg;
		ChannelBufferInputStream inStream = new ChannelBufferInputStream(buffer);
		Input in = new Input(inStream);
		return kryo.readObject(in, SegmentMessage.class);
	}
	
}
