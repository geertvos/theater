package net.geertvos.theater.api.actorstore;

import net.geertvos.theater.api.actors.Actor;
import net.geertvos.theater.api.actors.ActorId;

public interface ActorStore {

	void writeActor(int partition, Actor actor);
	
	Actor readActor(int partition, ActorId actorId);
	
}
