package net.geertvos.theater.api.management;

import net.geertvos.theater.api.actors.Actor;
import net.geertvos.theater.api.actors.ActorHandle;

public interface ActorSystem {

	void registerActor(Actor actor);
	
	void handleMessage(ActorHandle from, ActorHandle to, Object message);
	
}
