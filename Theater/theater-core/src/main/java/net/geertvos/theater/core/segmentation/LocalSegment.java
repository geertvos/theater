package net.geertvos.theater.core.segmentation;

import java.util.List;
import java.util.UUID;

import net.geertvos.theater.api.actors.Actor;
import net.geertvos.theater.api.actors.ActorId;
import net.geertvos.theater.api.actorstore.ActorStore;
import net.geertvos.theater.api.durability.MessageLog;
import net.geertvos.theater.api.factory.ActorFactory;
import net.geertvos.theater.api.messaging.Message;
import net.geertvos.theater.api.segmentation.Segment;
import net.geertvos.theater.core.durability.NoopMessageLog;
import net.geertvos.theater.core.util.ThreadBoundExecutorService;
import net.geertvos.theater.core.util.ThreadBoundRunnable;

import org.apache.log4j.Logger;

public class LocalSegment implements Segment {

	private final Logger LOG = Logger.getLogger(LocalSegment.class);
	private volatile boolean operational;
	private final ActorStore store;
	private final ActorFactory factory;
	private final int id;
	private final MessageLog log;
	private final ThreadBoundExecutorService<ThreadBoundRunnable<UUID>, UUID> executorService;
	private final UUID uniqueSegmentIdentifier = UUID.randomUUID();
	
	public LocalSegment(int id, ActorFactory factory, ActorStore store, MessageLog log,ThreadBoundExecutorService<ThreadBoundRunnable<UUID>, UUID> executor) {
		this.id = id;
		this.store = store;
		this.factory = factory;
		this.log = log;
		executorService = executor;
	}

	public LocalSegment(int id, ActorFactory factory, ActorStore store) {
		this(id,factory,store,new NoopMessageLog(), new ThreadBoundExecutorService<ThreadBoundRunnable<UUID>, UUID>(2));
	}

	public void handleMessage(final Message message) {
		
		ThreadBoundRunnable<UUID> handleMessage = new ThreadBoundRunnable<UUID>() {

			public void run() {
				if(operational && message.getFrom() == null && message.getType() == 1) {
					UUID upTo = UUID.fromString(message.getParameter("messageId"));
					replayLog(upTo);
					return;
				}
				log.logMessage(message);
				doHandleMessage(message);
			}

			public UUID getKey() {
				return uniqueSegmentIdentifier;
			}
		};
		executorService.submit(handleMessage);
	}
	
	private void doHandleMessage(final Message message) {
		if(operational) {
			
			ThreadBoundRunnable<UUID> handleMessage = new ThreadBoundRunnable<UUID>() {

				public void run() {
					ActorId actorId = message.getTo();
					Actor actor = store.readActor(id, actorId);
					if(actor == null) {
						actor = factory.createActor(message);
						actor.onCreate();
					}
					if(actor != null) {
						actor.onActivate();
						//TODO: remove the write here
						actor.handleMessage(message);
						store.writeActor(id, actor);
						log.ackMessage(message);
					}
				}

				public UUID getKey() {
					return message.getTo().getId();
				}
			};
			executorService.submit(handleMessage);
		}
	}

	public int getId() {
		return id;
	}
	
	public void onInit() {
		ThreadBoundRunnable<UUID> initTask = new ThreadBoundRunnable<UUID>() {

			public void run() {
				operational = true;
				LOG.info("Initializing local segment "+id);
				replayLog(null);
			}

			public UUID getKey() {
				return uniqueSegmentIdentifier;
			}
		};
		executorService.submit(initTask);
	}

	private void replayLog(UUID upTo) {
		List<Message> unacked = log.getUnackedMessages();
		if(unacked.size() > 0) {
			LOG.info("Replaying "+unacked.size()+" messages for segment "+id);
		}
		for(Message message : unacked) {
			if(upTo==null || message.getMessageId().compareTo(upTo) < 0) {
				if(message.getType()!=1) {
					doHandleMessage(message);
				}
			}
		}
	}

	public void onDestroy() {
		ThreadBoundRunnable<UUID> destroyTask = new ThreadBoundRunnable<UUID>() {

			public void run() {
				operational = false;
				LOG.info("Shutting down local segment "+id);
			}

			public UUID getKey() {
				return uniqueSegmentIdentifier;
			}
		};
		executorService.submit(destroyTask);
	}

	public boolean isLocal() {
		return true;
	}

	public boolean isOperational() {
		return operational;
	}

}
