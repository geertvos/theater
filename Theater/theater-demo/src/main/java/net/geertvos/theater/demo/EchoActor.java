package net.geertvos.theater.demo;

import net.geertvos.theater.api.actors.Actor;
import net.geertvos.theater.api.actors.ActorId;

public class EchoActor implements Actor {

	public Object onCreate(ActorId actor) {
		return null;
	}

	public void onActivate(ActorId actor, Object actorState) {
		// TODO Auto-generated method stub

	}

	public void onDeactivate(ActorId actor, Object actorState) {
		// TODO Auto-generated method stub

	}

	public void onDestroy(ActorId actor, Object actorState) {
		// TODO Auto-generated method stub

	}

	public void handleMessage(ActorId actor, ActorId from, Object message,
			Object actorState) {
		System.out.println("I received: "+message.toString());

	}

}
