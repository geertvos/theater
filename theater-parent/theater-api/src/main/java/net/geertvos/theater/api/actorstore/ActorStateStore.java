package net.geertvos.theater.api.actorstore;

import net.geertvos.theater.api.actors.ActorHandle;

public interface ActorStateStore {

	void writeActorState(ActorHandle handle, Object actorState);
	
	Object readActorState(ActorHandle handle);
	
}
