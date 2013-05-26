package net.geertvos.theater.api.management;

import net.geertvos.theater.api.actors.Actor;
import net.geertvos.theater.api.messaging.Message;

public interface ActorSystem {

	void registerActor(Actor actor, String type);
	
	void handleMessage(Message message);
	
}
