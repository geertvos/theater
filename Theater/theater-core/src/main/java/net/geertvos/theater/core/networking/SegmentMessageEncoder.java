package net.geertvos.theater.core.networking;

import java.util.UUID;

import net.geertvos.theater.core.actor.ActorIdImpl;
import net.geertvos.theater.core.serialization.UUIDSerializer;

import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.StackObjectPool;
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

	//Buffer size is chosen so small messages will fit.
	private static final int BUFFER_SIZE = 512;
	private final ObjectPool<Kryo> kryoPool;
	
	public SegmentMessageEncoder() {
			PoolableObjectFactory<Kryo> realFactory = new BasePoolableObjectFactory<Kryo>() {

				@Override
				public Kryo makeObject() throws Exception {
					Kryo kryo = new Kryo();
					kryo.register(SegmentMessage.class);
					kryo.register(ActorIdImpl.class);
					kryo.addDefaultSerializer(UUID.class, UUIDSerializer.class);
					return kryo;
				}
			};
			kryoPool = new StackObjectPool<Kryo>(realFactory);
	}
	
	
	@Override
	public Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
		Kryo kryo = kryoPool.borrowObject();
		SegmentMessage pm = (SegmentMessage)msg;
        Output out = new Output(BUFFER_SIZE, Integer.MAX_VALUE);
        kryo.writeObject(out, pm);
        kryoPool.returnObject(kryo);
        byte[] data = out.toBytes();
        return ChannelBuffers.wrappedBuffer(data);
	}

}
