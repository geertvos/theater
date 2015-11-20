package net.geertvos.theater.demo;

import net.geertvos.theater.api.actors.ActorHandle;
import net.geertvos.theater.core.actor.AbstractActorAdapter;

public class WordCountBoltActor extends AbstractActorAdapter {

	@Override
	public Object onCreate(ActorHandle actor) {
		return new WordCountBoltState();
	}

	
	public void onMessage(ActorHandle actor, ActorHandle from, Object message, Object actorState) {
		if(message instanceof LineMessage) {
			LineMessage l = (LineMessage)message;
			int wc = l.getLine().split(" ").length;
			LineResultMessage result = new LineResultMessage(l.getId(), wc);
			getTheater().sendMessage(actor, from, result);
			
			WordCountBoltState state = (WordCountBoltState)actorState;
			state.setLinesProcessed(state.getLinesProcessed()+1);
			System.out.println(actor.getId()+" splitted "+state.getLinesProcessed()+" lines.");
		}
	}

}
