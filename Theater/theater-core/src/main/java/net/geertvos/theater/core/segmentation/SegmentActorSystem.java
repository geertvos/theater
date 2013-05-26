package net.geertvos.theater.core.segmentation;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.geertvos.gossip.api.cluster.Cluster;
import net.geertvos.gossip.api.cluster.ClusterEventListener;
import net.geertvos.gossip.api.cluster.ClusterMember;
import net.geertvos.theater.api.actors.Actor;
import net.geertvos.theater.api.actors.ActorId;
import net.geertvos.theater.api.actorstore.ActorStateStore;
import net.geertvos.theater.api.durability.MessageLog;
import net.geertvos.theater.api.durability.SegmentMessageLogFactory;
import net.geertvos.theater.api.hashing.ConsistentHashFunction;
import net.geertvos.theater.api.management.ActorSystem;
import net.geertvos.theater.api.messaging.Message;
import net.geertvos.theater.api.segmentation.Segment;
import net.geertvos.theater.api.segmentation.SegmentManager;
import net.geertvos.theater.core.hashing.Md5HashFunction;
import net.geertvos.theater.core.util.ThreadBoundExecutorService;
import net.geertvos.theater.core.util.ThreadBoundRunnable;

import org.apache.log4j.Logger;

import com.esotericsoftware.minlog.Log;

public class SegmentActorSystem implements ActorSystem, SegmentManager, ClusterEventListener {

	private Logger logger = Logger.getLogger(SegmentActorSystem.class);
	private final ThreadBoundExecutorService<ThreadBoundRunnable<UUID>, UUID> executor = new ThreadBoundExecutorService<ThreadBoundRunnable<UUID>, UUID>(10);
	
	private final Cluster cluster;
	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	private final Lock readLock = lock.readLock();
	private final Lock writeLock = lock.writeLock();
	
	private final int numberOfSegments;
	private final List<Segment> segments;
	private final ActorStateStore store;
	private final SegmentMessageLogFactory messageLogFactory;
	private final Map<String,Actor> actors = new HashMap<String,Actor>();

	private ConsistentHashFunction hashFunction =  new Md5HashFunction();
	
	public SegmentActorSystem(SegmentMessageLogFactory factory, ActorStateStore store, int numberOfSegments, Cluster cluster) {
		this.messageLogFactory = factory;
		this.store = store;
		this.numberOfSegments = numberOfSegments;
		this.cluster = cluster;
		this.cluster.getEventService().registerListener(this);
		this.segments = new LinkedList<Segment>();
		for(int i=0;i<numberOfSegments;i++) {
			MessageLog log = factory.createLog(i);
			Segment p = new LocalSegment(i, this, store, log, executor);
			segments.add(p);
		}
	}

	public Segment findSegmentForActor(ActorId actor) {
		try {
			int hash = hash(actor.getId());
			int partition = hash % numberOfSegments;
			readLock.lock();
			return segments.get(partition);
		} finally {
			readLock.unlock();
		}
	}
	

	public void onNewActiveMember(ClusterMember member, List<ClusterMember> members) {
		Log.info("Cluster member "+member.getId()+" joined, redistributing segments.");
		updateSegment(members);
	}

	public void onNewInactiveMember(ClusterMember member, List<ClusterMember> members) {
	}

	public void onMemberActivated(ClusterMember member, List<ClusterMember> members) {
		Log.info("Cluster member "+member.getId()+" joined, redistributing segments.");
		updateSegment(members);
	}

	public void onMemberDeactivated(ClusterMember member, List<ClusterMember> members) {
		Log.info("Cluster member "+member.getId()+" left, redistributing segments.");
		updateSegment(members);
	}

	private void updateSegment(List<ClusterMember> members) {
		try {
			writeLock.lock();
			ClusterMember me = cluster.getLocalMember();
			int memberCount = members.size();
			for(int i=0;i<numberOfSegments;i++) {
				int memberNumber =  hash(i) % memberCount;
				ClusterMember member = members.get(memberNumber);
				if(member.getId().equals(me.getId())) {
					createLocalSegment(i);
				} else {
					createRemoteSegment(i, member);
				}
			}
			Log.info("Segments distributed, awaiting cluster stability.");
		} catch(Exception e) {
			Log.error("Error while updating segment distribution",e);
		} finally {
			writeLock.unlock();
		}
	}
	
	public void onClusterStabilized(List<ClusterMember> members) {
		try {
			readLock.lock();
			for(Segment p : segments) {
				if(!p.isOperational()) {
					Log.debug("Segment "+p.getId()+" initialized.");
					p.onInit();
				}
			}
		} catch(Exception e) {
			logger.error("Exception while initializing new segments.",e);
		} finally {
			readLock.unlock();
		}
		
	}
	
	private void createRemoteSegment(int i, ClusterMember member) {
		Segment current = segments.get(i);
		if(current instanceof RemoteSegment && ((RemoteSegment)current).getClusterMember().getId().equals(member.getId())) {
			logger.info("Segment "+i+" stays a remote segment on cluster member "+member.getId());
			return;
		}
		logger.info("Segment "+i+" relocates to remote cluster member "+member.getId());
		current.onDestroy();
		logger.debug("Segment "+i+" destroyed.");
		RemoteSegment newSegment = new RemoteSegment(i, member);
		segments.remove(i);
		segments.add(i, newSegment);
	}

	private void createLocalSegment(int i) {
		Segment current = segments.get(i);
		if(current.isLocal()) {
			logger.info("Segment "+i+" stays at local cluster member");
			return;
		}
		logger.info("Segment "+i+" relocates to local cluster member");
		current.onDestroy();
		logger.debug("Segment "+i+" destroyed.");
		MessageLog log = messageLogFactory.createLog(i);
		LocalSegment newSegment = new LocalSegment(i, this, store, log, executor);
		segments.remove(i);
		segments.add(i, newSegment);
	}

	private int hash(int number) {
		return hashFunction.hash(""+number);
	}
	
	private int hash(UUID id) {
		return hashFunction.hash(id.toString());
	}


	public void onClusterDestabilized(List<ClusterMember> members) {
		
	}

	public void setHashFunction(ConsistentHashFunction hashFunction) {
		this.hashFunction = hashFunction;
	}

	public void handleMessage(Message message) {
		findSegmentForActor(message.getTo()).handleMessage(message);
	}

	public Actor getActor(ActorId actorId) {
		return actors.get(actorId.getType());
	}

	public void registerActor(Actor actor, String type) {
		actors.put(type,actor);
	}

}
