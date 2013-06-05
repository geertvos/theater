package net.geertvos.theater.core.segmentation;

import java.util.List;
import java.util.UUID;

import net.geertvos.theater.api.actors.Actor;
import net.geertvos.theater.api.actors.ActorId;
import net.geertvos.theater.api.actorstore.ActorStateStore;
import net.geertvos.theater.api.durability.MessageLog;
import net.geertvos.theater.api.messaging.Message;
import net.geertvos.theater.api.segmentation.Segment;
import net.geertvos.theater.api.serialization.Deserializer;
import net.geertvos.theater.core.networking.SegmentMessageTypes;
import net.geertvos.theater.core.util.ThreadBoundExecutorService;
import net.geertvos.theater.core.util.ThreadBoundRunnable;
import net.geertvos.theater.kryo.serialization.KryoSerializer;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

public class LocalSegment implements Segment {

	private final Logger LOG = Logger.getLogger(LocalSegment.class);
	private volatile boolean operational;
	private final ActorStateStore store;
	private final SegmentActorSystem actorSystem;
	private final int id;
	private final MessageLog log;
	private final ThreadBoundExecutorService<ThreadBoundRunnable<UUID>, UUID> executorService;
	private final UUID uniqueSegmentIdentifier = UUID.randomUUID();
	private final Deserializer deserializer;
	
	public LocalSegment(int id, SegmentActorSystem actorSystem, ActorStateStore store, MessageLog log,ThreadBoundExecutorService<ThreadBoundRunnable<UUID>, UUID> executor) {
		this(id, actorSystem, store, log, executor, new KryoSerializer());
	}

	public LocalSegment(int id, SegmentActorSystem actorSystem, ActorStateStore store, MessageLog log,ThreadBoundExecutorService<ThreadBoundRunnable<UUID>, UUID> executor, Deserializer deserializer) {
		this.id = id;
		this.store = store;
		this.actorSystem = actorSystem;
		this.log = log;
		executorService = executor;
		this.deserializer = deserializer;
	}

	public void handleMessage(final Message message) {
		
		ThreadBoundRunnable<UUID> handleMessage = new ThreadBoundRunnable<UUID>() {

			public void run() {
				int type = message.getType();
				if(type == SegmentMessageTypes.LOG_REPLAY.ordinal() && operational) {
					LOG.info("Processing local message: "+message);
					UUID upTo = UUID.fromString(message.getParameter("messageId"));
					replayLog(upTo);
					return;
				} else if(type == SegmentMessageTypes.ACTOR_MESSAGE.ordinal()) {
					log.logMessage(message);
					doHandleMessage(message);
				} else {
					LOG.warn("Segment Message type "+type+" is not supported.");
				}
			}

			public UUID getKey() {
				return uniqueSegmentIdentifier;
			}
		};
		executorService.submit(handleMessage);
	}
	
	private void doHandleMessage(final Message message) {
			
			ThreadBoundRunnable<UUID> handleMessage = new ThreadBoundRunnable<UUID>() {

				public void run() {
					if(operational) {
						//TODO: create a payload decoder
						Object decodedMessage = null;
						String data = message.getParameter("payload");
						if(data != null) {
							byte[] bytes = Base64.decodeBase64(data);
							decodedMessage = deserializer.deserialize(bytes);
						} else {
							LOG.warn("Received message without payload.");
						}
						ActorId actorId = message.getTo();
						Actor actor = actorSystem.getActor(actorId);
						Object actorState = store.readActorState(id, actorId);
						if(actorState == null) {
							actorState = actor.onCreate(actorId);
						}
						if(actor != null) {
							actor.onActivate(actorId, actorState);
							actor.handleMessage(actorId, message.getFrom(), decodedMessage, actorState);
							//TODO: remove the write here
							actor.onDeactivate(actorId, actorState);
							store.writeActorState(id, actorId, actorState);
							log.ackMessage(message);
						}
					}
				}

				public UUID getKey() {
					return message.getTo().getId();
				}
			};
			executorService.submit(handleMessage);
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
			if(upTo!=null && !(message.getMessageId().compareTo(upTo) < 0)) {
				System.out.println("Got a replay message, older then current log.");
			}
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
