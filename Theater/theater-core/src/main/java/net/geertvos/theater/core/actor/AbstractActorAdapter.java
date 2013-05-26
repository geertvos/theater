package net.geertvos.theater.core.actor;

import net.geertvos.theater.api.actors.Actor;
import net.geertvos.theater.api.actors.ActorId;

public abstract class AbstractActorAdapter implements Actor {

	public Object onCreate(ActorId actor) {
		return null;
	}

	public void onActivate(ActorId actor, Object actorState) {
	}

	public void onDeactivate(ActorId actor, Object actorState) {
	}

	public void onDestroy(ActorId actor, Object actorState) {
	}


}
