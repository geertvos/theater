package net.geertvos.theater.demo;

import net.geertvos.theater.api.actors.ActorId;
import net.geertvos.theater.api.messaging.MessageSender;
import net.geertvos.theater.core.actor.AbstractActorAdapter;

import com.esotericsoftware.kryo.serializers.FieldSerializer.Optional;

public class WordCountBolt extends AbstractActorAdapter {

	@Optional("optional")
	private MessageSender sender;

	public WordCountBolt(MessageSender sender) {
		this.sender = sender;
	}
	
	public void handleMessage(ActorId actor, ActorId from, Object message, Object actorState) {
		if(message instanceof Line) {
			Line l = (Line)message;
			int wc = l.getLine().split(" ").length;
			LineResult result = new LineResult(l.getId(), wc);
			sender.sendMessage(actor, from, result);
		}
	}

}
