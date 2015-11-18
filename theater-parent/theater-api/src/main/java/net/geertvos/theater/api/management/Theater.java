package net.geertvos.theater.api.management;

import net.geertvos.theater.api.actors.ActorHandle;

public interface Theater {

	ActorSystem getActorSystem(String id);
	
	void registerActorSystem(String id, ActorSystem system);

	public void sendMessage(ActorHandle from, ActorHandle to, Object message);
	
}
