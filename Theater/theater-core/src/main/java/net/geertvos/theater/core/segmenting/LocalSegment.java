package net.geertvos.theater.core.segmenting;

import java.util.List;

import net.geertvos.theater.api.actors.Actor;
import net.geertvos.theater.api.actors.ActorId;
import net.geertvos.theater.api.actorstore.ActorStore;
import net.geertvos.theater.api.durability.MessageLog;
import net.geertvos.theater.api.events.ActorEventDispatcher;
import net.geertvos.theater.api.factory.ActorFactory;
import net.geertvos.theater.api.messaging.Message;
import net.geertvos.theater.api.partitioning.Segment;
import net.geertvos.theater.core.durability.NoopMessageLog;
import net.geertvos.theater.core.events.SynchronousActorEventDispatcher;

import org.apache.log4j.Logger;

public class LocalSegment implements Segment {

	private final Logger LOG = Logger.getLogger(LocalSegment.class);
	private volatile boolean operational;
	private final ActorStore store;
	private final ActorFactory factory;
	private final int id;
	private final MessageLog log;
	private final ActorEventDispatcher dispatcher = new SynchronousActorEventDispatcher();
	
	public LocalSegment(int id, ActorFactory factory, ActorStore store, MessageLog log) {
		this.id = id;
		this.store = store;
		this.factory = factory;
		this.log = log;
	}

	public LocalSegment(int id, ActorFactory factory, ActorStore store) {
		this(id,factory,store,new NoopMessageLog());
	}

	public void handleMessage(Message message) {
		log.logMessage(message);
		doHandleMessage(message);
	}
	
	private void doHandleMessage(Message message) {
		if(operational) {
			ActorId actorId = message.getTo();
			Actor actor = store.readActor(id, actorId);
			if(actor == null) {
				actor = factory.createActor(message);
				dispatcher.onCreate(actor);
			}
			if(actor != null) {
				dispatcher.onActivate(actor);
				//TODO: remove the write here
				dispatcher.onHandleMessage(actor, message);
				store.writeActor(id, actor);
				log.ackMessage(message);
			}
		}
	}

	public int getId() {
		return id;
	}
	
	public void onInit() {
		operational = true;
		LOG.info("Initializing local segment "+id);
		List<Message> unacked = log.getUnackedMessages();
		if(unacked.size() > 0) {
			LOG.info("Replaying "+unacked.size()+" messages for segment "+id);
		}
		for(Message message : unacked) {
			doHandleMessage(message);
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
