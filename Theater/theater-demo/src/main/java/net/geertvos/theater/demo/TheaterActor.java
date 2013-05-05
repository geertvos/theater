package net.geertvos.theater.demo;

import net.geertvos.theater.api.actors.ActorId;
import net.geertvos.theater.api.messaging.Message;
import net.geertvos.theater.core.actor.AbstractActorAdapter;

public class TheaterActor extends AbstractActorAdapter {

	private final ActorId id;

	public TheaterActor(ActorId id) {
		this.id = id;
	}
	
	public ActorId getId() {
		return id;
	}

	public void handleMessage(Message message) {
		String counter = message.getParameter("counter");
		System.out.println("Actor "+id+" - "+counter);
	}

}
