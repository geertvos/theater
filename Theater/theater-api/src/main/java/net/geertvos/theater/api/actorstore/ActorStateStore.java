package net.geertvos.theater.api.actorstore;

import net.geertvos.theater.api.actors.ActorId;

public interface ActorStateStore {

	void writeActorState(int partition, ActorId actorId, Object actorState);
	
	Object readActorState(int partition, ActorId actorId);
	
}
