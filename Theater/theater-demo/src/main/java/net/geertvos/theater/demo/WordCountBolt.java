package net.geertvos.theater.demo;

import net.geertvos.theater.api.actors.ActorId;
import net.geertvos.theater.api.messaging.MessageSender;
import net.geertvos.theater.core.actor.AbstractActorAdapter;

import com.esotericsoftware.kryo.serializers.FieldSerializer.Optional;

public class WordCountBolt extends AbstractActorAdapter {

	private MessageSender sender;

	public WordCountBolt(MessageSender sender) {
		this.sender = sender;
	}
	
	@Override
	public Object onCreate(ActorId actor) {
		return new WordCountBoltState();
	}

	
	public void handleMessage(ActorId actor, ActorId from, Object message, Object actorState) {
		if(message instanceof Line) {
			Line l = (Line)message;
			int wc = l.getLine().split(" ").length;
			LineResult result = new LineResult(l.getId(), wc);
			sender.sendMessage(actor, from, result);
			WordCountBoltState state = (WordCountBoltState)actorState;
			state.setLinesProcessed(state.getLinesProcessed()+1);
			System.out.println(actor.getId()+" splitted "+state.getLinesProcessed()+" lines.");
		}
	}

}
