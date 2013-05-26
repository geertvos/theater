package net.geertvos.theater.core.messaging;
import static net.geertvos.theater.core.networking.SegmentMessageTypes.ACTOR_MESSAGE;

import java.util.UUID;

import net.geertvos.theater.api.actors.ActorId;
import net.geertvos.theater.api.management.Theater;
import net.geertvos.theater.api.management.ActorSystem;
import net.geertvos.theater.api.messaging.MessageSender;
import net.geertvos.theater.api.serialization.Serializer;
import net.geertvos.theater.core.networking.SegmentMessage;
import net.geertvos.theater.core.util.UUIDGen;
import net.geertvos.theater.kryo.serialization.KryoSerializer;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

public class SegmentMessageSender implements MessageSender {

	private final static Logger log = Logger.getLogger(SegmentMessageSender.class);

	private Theater cluster;
	private final Serializer serializer;
	
	//TODO: Remove this class completely and built it in the appropriate place
	public SegmentMessageSender(Theater cluster) {
		this(cluster, new KryoSerializer());
	}
	
	public SegmentMessageSender(Theater cluster, Serializer serializer) {
		this.serializer = serializer;
		this.cluster = cluster;
	}
	
	public void sendMessage(ActorId from, ActorId to, Object message) {
		ActorSystem system = cluster.getActorSystem(to.getSystem());
		if(system == null) {
			log.error("Received a message for an non existing system. Please register the following system: "+to.getSystem());
			return;
		}
		UUID messageId = UUIDGen.makeType1UUIDFromHost(UUIDGen.getLocalAddress());
		SegmentMessage internalMessage = new SegmentMessage(ACTOR_MESSAGE.ordinal(), messageId, from, to);
		if(message != null) {
			byte[] unEncodedData = serializer.serialize(message);
			if(unEncodedData != null) {
				String data = Base64.encodeBase64String(unEncodedData);
				internalMessage.setParameter("payload", data);
			} else {
				log.error("Serialization of message failed.");
				return;
			}
		} else {
			log.warn("Sending message without payload.");
		}
		system.handleMessage(internalMessage);
	}
	
}
