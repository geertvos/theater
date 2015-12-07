package net.geertvos.theater.core.actor;

import net.geertvos.theater.api.actors.Actor;
import net.geertvos.theater.api.actors.ActorHandle;
import net.geertvos.theater.api.management.Theater;

public abstract class AbstractActorAdapter<S> implements Actor<S> {

	protected Theater theater;

	public void setTheater(Theater theater) {
		this.theater = theater;
	}
	
	protected Theater getTheater() {
		return theater;
	}
	
	public S onCreate(ActorHandle actor) {
		return null;
	}

	public void onActivate(ActorHandle actor, S actorState) {
	}

	public void onDeactivate(ActorHandle actor, S actorState) {
	}

	public void onDestroy(ActorHandle actor, S actorState) {
	}


}
