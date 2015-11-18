package net.geertvos.theater.core.segmentation;

import java.util.UUID;

import org.apache.log4j.Logger;

import net.geertvos.theater.api.actors.Actor;
import net.geertvos.theater.api.actors.ActorHandle;
import net.geertvos.theater.api.actorstore.ActorStateStore;
import net.geertvos.theater.api.segmentation.Segment;
import net.geertvos.theater.api.serialization.Deserializer;
import net.geertvos.theater.core.util.ThreadBoundExecutorService;
import net.geertvos.theater.core.util.ThreadBoundRunnable;
import net.geertvos.theater.kryo.serialization.KryoSerializer;

public class LocalSegment implements Segment {

	private final Logger LOG = Logger.getLogger(LocalSegment.class);
	private volatile boolean operational;
	private final ActorStateStore store;
	private final SegmentedActorSystem actorSystem;
	private final int id;
	private final ThreadBoundExecutorService<ThreadBoundRunnable<UUID>, UUID> executorService;
	private final UUID uniqueSegmentIdentifier = UUID.randomUUID();
	
	public LocalSegment(int id, SegmentedActorSystem actorSystem, ActorStateStore store, ThreadBoundExecutorService<ThreadBoundRunnable<UUID>, UUID> executor) {
		this(id, actorSystem, store, executor, new KryoSerializer());
	}

	public LocalSegment(int id, SegmentedActorSystem actorSystem, ActorStateStore store, ThreadBoundExecutorService<ThreadBoundRunnable<UUID>, UUID> executor, Deserializer deserializer) {
		this.id = id;
		this.store = store;
		this.actorSystem = actorSystem;
		executorService = executor;
	}

	public void handleMessage(final ActorHandle from, final ActorHandle to, final Object message) {
		LOG.info("LocalSegment is handling message from "+from+" to "+to);
		doHandleMessage(from, to, message);
	}
	
	private void doHandleMessage(final ActorHandle from, final ActorHandle to, final Object message) {
			
			ThreadBoundRunnable<UUID> handleMessage = new ThreadBoundRunnable<UUID>() {

				public void run() {
					if(operational) {
						Actor actor = actorSystem.getActor(to);
						Object actorState = store.readActorState(to);
						if(actorState == null) {
							actorState = actor.onCreate(to);
						}
						if(actor != null) {
							actor.onActivate(to, actorState);
							actor.onMessage(to, from, message, actorState);
							actor.onDeactivate(to, actorState);
							store.writeActorState(to, actorState);
						}
					}
				}

				public UUID getKey() {
					return to.getId();
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
			}

			public UUID getKey() {
				return uniqueSegmentIdentifier;
			}
		};
		executorService.submit(initTask);
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
