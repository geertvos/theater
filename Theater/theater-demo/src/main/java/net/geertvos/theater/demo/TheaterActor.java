package net.geertvos.theater.demo;

import net.geertvos.theater.api.actors.Actor;
import net.geertvos.theater.api.actors.ActorId;
import net.geertvos.theater.api.messaging.Message;
import net.geertvos.theater.core.actor.AbstractActorAdapter;

public class TheaterActor extends AbstractActorAdapter implements Actor {

	private ActorId id;
	private long counter = 0;

	public TheaterActor() {
	}
	
	public TheaterActor(ActorId id) {
		this.id = id;
	}
	
	public void setId(ActorId id) {
		this.id = id;
	}
	
	public ActorId getId() {
		return id;
	}
	
	public long getCounter() {
		return counter;
	}
	
	public void setCounter(long counter) {
		this.counter = counter;
	}

	public void handleMessage(Message message) {
		String counter = message.getParameter("counter");
		System.out.println("Actor "+id+" - "+counter+" internal counter "+this.counter);
		this.counter++;
	}

}
