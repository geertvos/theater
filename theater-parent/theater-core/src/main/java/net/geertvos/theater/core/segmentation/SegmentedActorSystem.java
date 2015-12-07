package net.geertvos.theater.core.segmentation;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.geertvos.theater.api.actors.Actor;
import net.geertvos.theater.api.actors.ActorHandle;
import net.geertvos.theater.api.actorstore.ActorStateStore;
import net.geertvos.theater.api.clustering.GroupMember;
import net.geertvos.theater.api.clustering.GroupMembershipListener;
import net.geertvos.theater.api.clustering.GroupMembershipProvider;
import net.geertvos.theater.api.hashing.HashFunction;
import net.geertvos.theater.api.management.ActorSystem;
import net.geertvos.theater.api.management.Theater;
import net.geertvos.theater.api.segmentation.Segment;
import net.geertvos.theater.api.segmentation.SegmentManager;
import net.geertvos.theater.core.actor.AbstractActorAdapter;
import net.geertvos.theater.core.hashing.Md5HashFunction;
import net.geertvos.theater.core.networking.SegmentClient;
import net.geertvos.theater.core.networking.SegmentClientFactory;
import net.geertvos.theater.core.util.ThreadBoundExecutorService;
import net.geertvos.theater.core.util.ThreadBoundRunnable;

import org.apache.log4j.Logger;

import com.esotericsoftware.minlog.Log;

public class SegmentedActorSystem implements ActorSystem, SegmentManager, GroupMembershipListener {

	private static final Logger LOG = Logger.getLogger(SegmentedActorSystem.class);
	private final ThreadBoundExecutorService<ThreadBoundRunnable<UUID>, UUID> executor = new ThreadBoundExecutorService<ThreadBoundRunnable<UUID>, UUID>(10);
	private final GroupMembershipProvider cluster;
	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	private final Lock readLock = lock.readLock();
	private final Lock writeLock = lock.writeLock();
	private final int numberOfSegments;
	private final List<Segment> segments;
	private final ActorStateStore store;
	private final SegmentClientFactory clientFactory;
	private final Theater theater;
	private final HashFunction hashFunction =  new Md5HashFunction();
	
	public SegmentedActorSystem(Theater theater, ActorStateStore store, int numberOfSegments, GroupMembershipProvider cluster, SegmentClientFactory clientFactory) {
		this.theater = theater;
		this.store = store;
		this.numberOfSegments = numberOfSegments;
		this.cluster = cluster;
		this.cluster.registerListener(this);
		this.segments = new LinkedList<Segment>();
		this.clientFactory = clientFactory;
		for(int i=0;i<numberOfSegments;i++) {
			Segment p = new LocalSegment(i, this, store, executor);
			segments.add(p);
		}
	}

	public Segment findSegmentForActor(ActorHandle actor) {
		try {
			int hash = hash(actor.getId());
			int partition = hash % numberOfSegments;
			readLock.lock();
			return segments.get(partition);
		} finally {
			readLock.unlock();
		}
	}
	

	public void onNewActiveMember(GroupMember member, List<GroupMember> members) {
		Log.info("Cluster member "+member.getId()+" joined, redistributing segments.");
		updateSegment(members);
	}

	public void onNewInactiveMember(GroupMember member, List<GroupMember> members) {
	}

	public void onMemberActivated(GroupMember member, List<GroupMember> members) {
		Log.info("Cluster member "+member.getId()+" joined, redistributing segments.");
		updateSegment(members);
	}

	public void onMemberDeactivated(GroupMember member, List<GroupMember> members) {
		Log.info("Cluster member "+member.getId()+" left, redistributing segments.");
		updateSegment(members);
	}

	private void updateSegment(List<GroupMember> members) {
		try {
			writeLock.lock();
			GroupMember me = cluster.getLocalMember();
			int memberCount = members.size();
			for(int i=0;i<numberOfSegments;i++) {
				int memberNumber =  hash(i) % memberCount;
				GroupMember member = members.get(memberNumber);
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
	
	public void onClusterStabilized(List<GroupMember> members) {
		try {
			readLock.lock();
			for(Segment p : segments) {
				if(!p.isOperational()) {
					Log.debug("Segment "+p.getId()+" initialized.");
					p.onInit();
				}
			}
		} catch(Exception e) {
			LOG.error("Exception while initializing new segments.",e);
		} finally {
			readLock.unlock();
		}
		
	}
	
	private void createRemoteSegment(int i, GroupMember member) {
		Segment current = segments.get(i);
		if(current instanceof RemoteSegment && ((RemoteSegment)current).getClusterMember().getId().equals(member.getId())) {
			LOG.info("Segment "+i+" stays a remote segment on cluster member "+member.getId());
			return;
		}
		LOG.info("Segment "+i+" relocates to remote cluster member "+member.getId());
		current.onDestroy();
		LOG.debug("Segment "+i+" destroyed.");

		SegmentClient client = clientFactory.createClient(i, member);
		RemoteSegment newSegment = new RemoteSegment(i, member, client);
		segments.remove(i);
		segments.add(i, newSegment);
	}

	private void createLocalSegment(int i) {
		Segment current = segments.get(i);
		if(current.isLocal()) {
			LOG.info("Segment "+i+" stays at local cluster member");
			return;
		}
		LOG.info("Segment "+i+" relocates to local cluster member");
		current.onDestroy();
		LOG.debug("Segment "+i+" destroyed.");
		LocalSegment newSegment = new LocalSegment(i, this, store, executor);
		segments.remove(i);
		segments.add(i, newSegment);
	}

	private int hash(int number) {
		return hashFunction.hash(""+number);
	}
	
	private int hash(UUID id) {
		return hashFunction.hash(id.toString());
	}


	public void onClusterDestabilized(List<GroupMember> members) {
		
	}

	public Actor getActor(ActorHandle actorHandle) {
		try {
			AbstractActorAdapter actor = (AbstractActorAdapter) Class.forName(actorHandle.getType()).newInstance();
			actor.setTheater(theater);
			return actor;
		} catch(ClassNotFoundException e) {
			Log.error("Trying to obtain reference to class: "+actorHandle.getType()+", but class not found.", e);
		} catch (InstantiationException e) {
			Log.error("Trying to obtain reference to class: "+actorHandle.getType()+", but could not instantiate.", e);
		} catch (IllegalAccessException e) {
			Log.error("Trying to obtain reference to class: "+actorHandle.getType()+", but got illegal access.", e);
		}
		return null;
	}

	public void handleMessage(ActorHandle from, ActorHandle to, Object message) {
		findSegmentForActor(to).handleMessage(from, to, message);
	}

}
