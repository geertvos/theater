import java.util.UUID;

import net.geertvos.theater.api.actors.ActorId;
import net.geertvos.theater.core.actor.ActorIdImpl;
import net.geertvos.theater.core.networking.PartitionMessage;
import net.geertvos.theater.core.networking.PartitionMessageDecoder;
import net.geertvos.theater.core.networking.PartitionMessageEncoder;

import org.testng.annotations.Test;


public class PartitionMessageEncoderTest {

	@Test( groups = "unitTest")
	public void testEncoder() throws Exception {
		PartitionMessageEncoder encoder = new PartitionMessageEncoder();
		PartitionMessageDecoder decoder = new PartitionMessageDecoder();
		
		ActorId from = new ActorIdImpl(UUID.randomUUID(), "this");
		ActorId to = new ActorIdImpl(UUID.randomUUID(), "this");
		PartitionMessage message = new PartitionMessage(from, to);
		message.setParameter("test", "value");
		
		Object o = encoder.encode(null, null, message);
		System.out.println(o);
		decoder.decode(null, null, o);
	}
	
}
