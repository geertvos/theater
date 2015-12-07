package net.geertvos.theater.demo.bolts;

import net.geertvos.theater.api.actors.ActorHandle;
import net.geertvos.theater.core.actor.AbstractActorAdapter;
import net.geertvos.theater.demo.messages.LineMessage;
import net.geertvos.theater.demo.messages.LineResultMessage;

public class WordCountBoltActor extends AbstractActorAdapter<WordCountBoltState> {

	@Override
	public WordCountBoltState onCreate(ActorHandle actor) {
		return new WordCountBoltState();
	}

	public void onMessage(ActorHandle actor, ActorHandle from, Object message, WordCountBoltState state) {
		if(message instanceof LineMessage) {

			LineMessage l = (LineMessage)message;
			int wc = l.getLine().split(" ").length;
			LineResultMessage result = new LineResultMessage(l.getId(), wc);

			getTheater().sendMessage(actor, from, result);
			
			state.setLinesProcessed(state.getLinesProcessed()+1);
			
			System.out.println(actor.getId()+" splitted "+state.getLinesProcessed()+" lines.");
		}
	}

}
