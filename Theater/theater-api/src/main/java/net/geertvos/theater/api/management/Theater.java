package net.geertvos.theater.api.management;

public interface Theater {

	ActorSystem getActorSystem(String id);
	
	void registerActorSystem(String id, ActorSystem system);
	
}
