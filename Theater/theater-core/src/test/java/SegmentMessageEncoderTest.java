import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.UUID;

import net.geertvos.theater.api.actors.ActorId;
import net.geertvos.theater.core.actor.ActorIdImpl;
import net.geertvos.theater.core.networking.SegmentMessage;
import net.geertvos.theater.core.networking.SegmentMessageDecoder;
import net.geertvos.theater.core.networking.SegmentMessageEncoder;
import net.geertvos.theater.core.serialization.UUIDSerializer;

import org.testng.annotations.Test;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.ByteBufferInputStream;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;


public class SegmentMessageEncoderTest {

	@Test( groups = "unitTest")
	public void testEncoder() throws Exception {
		SegmentMessageEncoder encoder = new SegmentMessageEncoder();
		SegmentMessageDecoder decoder = new SegmentMessageDecoder();
		
		ActorId from = new ActorIdImpl(UUID.randomUUID(), "this");
		ActorId to = new ActorIdImpl(UUID.randomUUID(), "this");
		SegmentMessage message = new SegmentMessage(1,UUID.randomUUID(),from,to);
		message.setParameter("test", "value");
		
		Object o = encoder.encode(null, null, message);
		System.out.println(o);
		decoder.decode(null, null, o);
	}
	
	@Test( groups = "unitTest")
	public void extraTest() {
		Kryo kryo = new Kryo();
		kryo.addDefaultSerializer(UUID.class, UUIDSerializer.class);
		ActorId from = new ActorIdImpl(UUID.randomUUID(), "this");
		ActorId to = new ActorIdImpl(UUID.randomUUID(), "this");
		SegmentMessage message = new SegmentMessage(1,UUID.randomUUID(),from,to);
		message.setParameter("test", "value");

        Output out = new Output(4096);
        kryo.writeObject(out, message);
        ByteBuffer buffer = ByteBuffer.wrap(out.getBuffer());
		buffer.rewind();
		ByteBufferInputStream inStream = new ByteBufferInputStream(buffer);
		Input in = new Input(inStream);
		SegmentMessage m2 = kryo.readObject(in, SegmentMessage.class);
	}
	
}
