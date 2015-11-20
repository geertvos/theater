package net.geertvos.theater.demo;

import net.geertvos.theater.api.actors.ActorHandle;
import net.geertvos.theater.core.actor.AbstractActorAdapter;

public class EchoActor extends AbstractActorAdapter {

	public void onMessage(ActorHandle actor, ActorHandle from, Object message, Object actorState) {
		System.out.println("I received: "+message.toString());

	}

}
