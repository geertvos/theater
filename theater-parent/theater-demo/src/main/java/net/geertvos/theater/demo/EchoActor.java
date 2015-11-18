package net.geertvos.theater.demo;

import net.geertvos.theater.api.actors.Actor;
import net.geertvos.theater.api.actors.ActorHandle;
import net.geertvos.theater.api.management.ActorSystem;

public class EchoActor implements Actor {

	public Object onCreate(ActorHandle actor) {
		return null;
	}

	public void onActivate(ActorHandle actor, Object actorState) {
		// TODO Auto-generated method stub

	}

	public void onDeactivate(ActorHandle actor, Object actorState) {
		// TODO Auto-generated method stub

	}

	public void onDestroy(ActorHandle actor, Object actorState) {
		// TODO Auto-generated method stub

	}

	public void onMessage(ActorHandle actor, ActorHandle from, Object message, Object actorState) {
		System.out.println("I received: "+message.toString());

	}

	public String getType() {
		return "echo";
	}

}
