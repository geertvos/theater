package net.geertvos.theater.core.networking.netty;

import java.io.IOException;

import net.geertvos.theater.api.management.ActorSystem;
import net.geertvos.theater.api.management.Theater;
import net.geertvos.theater.api.serialization.Deserializer;
import net.geertvos.theater.core.networking.SegmentMessage;
import net.geertvos.theater.kryo.serialization.KryoSerializer;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

public class SegmentMessageHandler extends SimpleChannelHandler {

	private Logger log = Logger.getLogger(SegmentMessageHandler.class);
	private final Theater actorCluster;
	private final Deserializer deserializer = new KryoSerializer();
	
	public SegmentMessageHandler(Theater manager) {
		this.actorCluster = manager;
	}
	
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		SegmentMessage message = (SegmentMessage) e.getMessage();
		log.info("Received a segment message from "+message.getFrom());
		ActorSystem system = actorCluster.getActorSystem(message.getTo().getSystem());
		
		String data = message.getParameter("payload");
		if(data != null) {
			byte[] bytes = Base64.decodeBase64(data);
			Object decodedMessage = deserializer.deserialize(bytes);
			system.handleMessage(message.getFrom(), message.getTo(), decodedMessage);
		} else {
			log.warn("Received message without payload.");
		}
		
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		if(e.getCause() instanceof IOException) {
			//ignore for now, we need to handle failed connections later.
		} else {
			log.error(e);
		}
	}
	
}
