package net.geertvos.theater.demo;

import net.geertvos.theater.api.actors.ActorHandle;
import net.geertvos.theater.core.actor.StatelessAbstractActorAdapter;

public class EchoActor extends StatelessAbstractActorAdapter {

	public void onMessage(ActorHandle actor, ActorHandle from, Object message) {
		System.out.println("I received: "+message.toString());

	}

}
