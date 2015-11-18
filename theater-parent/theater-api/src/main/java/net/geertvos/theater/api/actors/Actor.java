package net.geertvos.theater.api.actors;

public interface Actor {

	Object onCreate(ActorHandle actor);
	
	void onActivate(ActorHandle actor, Object actorState);
	
	void onDeactivate(ActorHandle actor, Object actorState);
	
	void onDestroy(ActorHandle actor, Object actorState);
	
	void onMessage(ActorHandle actor, ActorHandle from, Object message, Object actorState);
	
	String getType();
	
}
