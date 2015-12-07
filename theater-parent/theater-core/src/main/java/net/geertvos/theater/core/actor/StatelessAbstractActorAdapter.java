package net.geertvos.theater.core.actor;

import net.geertvos.theater.api.actors.Actor;
import net.geertvos.theater.api.actors.ActorHandle;
import net.geertvos.theater.api.management.Theater;

public abstract class StatelessAbstractActorAdapter implements Actor<Void> {

	protected Theater theater;

	public void setTheater(Theater theater) {
		this.theater = theater;
	}
	
	protected Theater getTheater() {
		return theater;
	}
	
	public Void onCreate(ActorHandle actor) {
		return null;
	}

	public void onActivate(ActorHandle actor) {
	}

	public void onDeactivate(ActorHandle actor) {
	}

	public void onDestroy(ActorHandle actor) {
	}

	public void onActivate(ActorHandle actor, Void actorState) {
		onActivate(actor);
	}

	public void onDeactivate(ActorHandle actor, Void actorState) {
		onDeactivate(actor);
	}

	public void onDestroy(ActorHandle actor, Void actorState) {
		onDestroy(actor);
	}

	public void onMessage(ActorHandle actor, ActorHandle from, Object message, Void actorState) {
		onMessage(actor, from, message);
	}

	public abstract void onMessage(ActorHandle actor, ActorHandle from, Object message);

}
