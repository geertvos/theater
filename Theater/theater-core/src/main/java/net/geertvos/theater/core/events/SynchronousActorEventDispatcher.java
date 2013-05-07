package net.geertvos.theater.core.events;

import net.geertvos.theater.api.actors.Actor;
import net.geertvos.theater.api.events.ActorEventDispatcher;
import net.geertvos.theater.api.messaging.Message;

public class SynchronousActorEventDispatcher implements ActorEventDispatcher {

	public void onCreate(Actor actor) {
		actor.onCreate();
	}

	public void onActivate(Actor actor) {
		actor.onActivate();
	}

	public void onDeactivate(Actor actor) {
		actor.onDeactivate();
	}

	public void onHandleMessage(Actor actor, Message message) {
		actor.handleMessage(message);
	}

	public void onDestroy(Actor actor) {
		actor.onDestroy();
	}

}
