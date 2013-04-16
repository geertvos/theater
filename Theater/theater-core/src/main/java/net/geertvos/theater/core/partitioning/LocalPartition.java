package net.geertvos.theater.core.partitioning;

import net.geertvos.theater.api.actors.Actor;
import net.geertvos.theater.api.actors.ActorId;
import net.geertvos.theater.api.actorstore.ActorStore;
import net.geertvos.theater.api.factory.ActorFactory;
import net.geertvos.theater.api.messaging.Message;
import net.geertvos.theater.api.partitioning.Partition;

public class LocalPartition implements Partition {

	private volatile boolean operational;
	private final ActorStore store;
	private final ActorFactory factory;
	private final int id;
	
	public LocalPartition(int id, ActorFactory factory, ActorStore store) {
		this.id = id;
		this.store = store;
		this.factory = factory;
	}

	public void handleMessage(Message message) {
		if(operational) {
			ActorId actorId = message.getTo();
			Actor actor = store.readActor(actorId);
			if(actor == null) {
				actor = factory.createActor(message);
			}
			if(actor != null) {
				//TODO probably we want to execute this on an actorexecutor
				actor.handleMessage(message);
			}
		}
	}

	public int getId() {
		return id;
	}

	public void onInit() {
		operational = true;
	}

	public void onDestroy() {
		operational = false;
	}

}
