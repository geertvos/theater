package net.geertvos.theater.core.partitioning;

import net.geertvos.theater.api.actors.Actor;
import net.geertvos.theater.api.actors.ActorId;
import net.geertvos.theater.api.actorstore.ActorStore;
import net.geertvos.theater.api.durability.PartitionMessageLog;
import net.geertvos.theater.api.factory.ActorFactory;
import net.geertvos.theater.api.messaging.Message;
import net.geertvos.theater.api.partitioning.Partition;
import net.geertvos.theater.core.durability.NoopPartitionMessageLog;

public class LocalPartition implements Partition {

	private volatile boolean operational;
	private final ActorStore store;
	private final ActorFactory factory;
	private final int id;
	private final PartitionMessageLog log;
	
	public LocalPartition(int id, ActorFactory factory, ActorStore store, PartitionMessageLog log) {
		this.id = id;
		this.store = store;
		this.factory = factory;
		this.log = log;
	}

	public LocalPartition(int id, ActorFactory factory, ActorStore store) {
		this(id,factory,store,new NoopPartitionMessageLog());
	}

	public void handleMessage(Message message) {
		if(operational) {
			ActorId actorId = message.getTo();
			Actor actor = store.readActor(actorId);
			if(actor == null) {
				actor = factory.createActor(message);
			}
			if(actor != null) {
				store.writeActor(actor);
				actor.handleMessage(message);
				log.ackMessage(message);
			}
		} else {
			throw new IllegalStateException("Partition is not yet initialized.");
		}
	}

	public int getId() {
		return id;
	}

	public void onInit() {
		operational = true;
		for(Message message : log.getUnackedMessages()) {
			handleMessage(message);
			log.ackMessage(message);
		}
	}

	public void onDestroy() {
		operational = false;
	}

}
