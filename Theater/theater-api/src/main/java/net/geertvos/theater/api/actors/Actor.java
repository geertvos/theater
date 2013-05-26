package net.geertvos.theater.api.actors;


public interface Actor {

	Object onCreate(ActorId actor);
	
	void onActivate(ActorId actor, Object actorState);
	
	void onDeactivate(ActorId actor, Object actorState);
	
	void onDestroy(ActorId actor, Object actorState);
	
	void handleMessage(ActorId actor, ActorId from,Object message, Object actorState);
	
}
