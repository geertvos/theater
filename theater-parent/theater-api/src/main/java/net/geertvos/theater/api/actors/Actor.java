package net.geertvos.theater.api.actors;

import net.geertvos.theater.api.management.Theater;

public interface Actor<S> {

	S onCreate(ActorHandle actor);
	
	void onActivate(ActorHandle actor, S actorState);
	
	void onDeactivate(ActorHandle actor, S actorState);
	
	void onDestroy(ActorHandle actor, S actorState);
	
	void onMessage(ActorHandle actor, ActorHandle from, Object message, S actorState);

	void setTheater(Theater theater);
	
}
