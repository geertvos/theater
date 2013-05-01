package net.geertvos.theater.core.networking;

import java.nio.ByteBuffer;
import java.util.UUID;

import net.geertvos.theater.core.actor.ActorIdImpl;
import net.geertvos.theater.core.serialization.UUIDSerializer;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.ByteBufferInputStream;
import com.esotericsoftware.kryo.io.Input;

/**
 * @author Geert Vos
 */
public class PartitionMessageDecoder extends OneToOneDecoder {

	private Kryo kryo;
	
	public PartitionMessageDecoder() {
		kryo = new Kryo();
		kryo.register(PartitionMessage.class);
		kryo.register(ActorIdImpl.class);
		kryo.addDefaultSerializer(UUID.class, UUIDSerializer.class);
	}
	
	@Override
	public Object decode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
		ByteBuffer buffer = (ByteBuffer)msg;
		ByteBufferInputStream inStream = new ByteBufferInputStream(buffer);
		Input in = new Input(inStream);
		return kryo.readObject(in, PartitionMessage.class);
	}
	
}
