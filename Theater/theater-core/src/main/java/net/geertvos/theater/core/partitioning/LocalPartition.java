package net.geertvos.theater.core.partitioning;

import java.util.List;

import org.apache.log4j.Logger;

import net.geertvos.theater.api.actors.Actor;
import net.geertvos.theater.api.actors.ActorId;
import net.geertvos.theater.api.actorstore.ActorStore;
import net.geertvos.theater.api.durability.MessageLog;
import net.geertvos.theater.api.factory.ActorFactory;
import net.geertvos.theater.api.messaging.Message;
import net.geertvos.theater.api.partitioning.Partition;
import net.geertvos.theater.core.durability.NoopPartitionMessageLog;

public class LocalPartition implements Partition {

	private final Logger LOG = Logger.getLogger(LocalPartition.class);
	private volatile boolean operational;
	private final ActorStore store;
	private final ActorFactory factory;
	private final int id;
	private final MessageLog log;
	
	public LocalPartition(int id, ActorFactory factory, ActorStore store, MessageLog log) {
		this.id = id;
		this.store = store;
		this.factory = factory;
		this.log = log;
	}

	public LocalPartition(int id, ActorFactory factory, ActorStore store) {
		this(id,factory,store,new NoopPartitionMessageLog());
	}

	public void handleMessage(Message message) {
		log.logMessage(message);
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
		}
	}

	public int getId() {
		return id;
	}
	
	public void onInit() {
		operational = true;
		LOG.info("Initializing partition "+id);
		List<Message> unacked = log.getUnackedMessages();
		if(unacked.size() > 0) {
			LOG.info("Replaying "+unacked.size()+" messages for partition "+id);
		}
		for(Message message : unacked) {
			handleMessage(message);
			log.ackMessage(message);
		}
	}

	public void onDestroy() {
		operational = false;
	}

	public boolean isLocal() {
		return true;
	}

	public boolean isOperational() {
		return operational;
	}

}
