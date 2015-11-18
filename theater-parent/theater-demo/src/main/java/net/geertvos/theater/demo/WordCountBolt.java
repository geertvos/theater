package net.geertvos.theater.demo;

import net.geertvos.theater.api.actors.ActorHandle;
import net.geertvos.theater.api.management.Theater;
import net.geertvos.theater.core.actor.AbstractActorAdapter;

public class WordCountBolt extends AbstractActorAdapter {

	public WordCountBolt(Theater theater) {
		super(theater);
	}
	
	@Override
	public Object onCreate(ActorHandle actor) {
		return new WordCountBoltState();
	}

	
	public void onMessage(ActorHandle actor, ActorHandle from, Object message, Object actorState) {
		if(message instanceof Line) {
			Line l = (Line)message;
			int wc = l.getLine().split(" ").length;
			LineResult result = new LineResult(l.getId(), wc);
			getTheater().sendMessage(actor, from, result);
			
			WordCountBoltState state = (WordCountBoltState)actorState;
			state.setLinesProcessed(state.getLinesProcessed()+1);
			System.out.println(actor.getId()+" splitted "+state.getLinesProcessed()+" lines.");
		}
	}

	public String getType() {
		return "wordcountbolt";
	}

}
