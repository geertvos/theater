package net.geertvos.theater.core.actor;

import net.geertvos.theater.api.actors.Actor;
import net.geertvos.theater.api.actors.ActorHandle;
import net.geertvos.theater.api.management.Theater;

public abstract class AbstractActorAdapter implements Actor {

	protected Theater theater;

	protected AbstractActorAdapter(Theater theater) {
		this.theater = theater;
	}
	
	protected Theater getTheater() {
		return theater;
	}
	
	public Object onCreate(ActorHandle actor) {
		return null;
	}

	public void onActivate(ActorHandle actor, Object actorState) {
	}

	public void onDeactivate(ActorHandle actor, Object actorState) {
	}

	public void onDestroy(ActorHandle actor, Object actorState) {
	}


}
