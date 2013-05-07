package net.geertvos.theater.api.events;

import net.geertvos.theater.api.actors.Actor;
import net.geertvos.theater.api.messaging.Message;

public interface ActorEventDispatcher {

	void onCreate(Actor actor);

	void onActivate(Actor actor);

	void onDeactivate(Actor actor);

	void onHandleMessage(Actor actor, Message message);

	void onDestroy(Actor actor);
	
}
