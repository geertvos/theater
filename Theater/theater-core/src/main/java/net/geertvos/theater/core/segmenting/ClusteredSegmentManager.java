package net.geertvos.theater.core.segmenting;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.geertvos.gossip.api.cluster.Cluster;
import net.geertvos.gossip.api.cluster.ClusterEventListener;
import net.geertvos.gossip.api.cluster.ClusterMember;
import net.geertvos.theater.api.actors.ActorId;
import net.geertvos.theater.api.hashing.ConsistentHashFunction;
import net.geertvos.theater.api.partitioning.Segment;
import net.geertvos.theater.api.partitioning.SegmentManager;
import net.geertvos.theater.core.hashing.FakeHashFunction;

import org.apache.log4j.Logger;

import com.esotericsoftware.minlog.Log;

public class ClusteredSegmentManager implements SegmentManager, ClusterEventListener {

	private Logger logger = Logger.getLogger(ClusteredSegmentManager.class);
	
	private final Cluster cluster;
	private final LocalSegmentFactory localSegmentFactory;
	private final RemoteSegmentFactory remoteSegmentFactory;
	
	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	private final Lock readLock = lock.readLock();
	private final Lock writeLock = lock.writeLock();
	
	private final int numberOfSegments;
	private final List<Segment> segments;
	
	private ConsistentHashFunction hashFunction =  new FakeHashFunction();
	
	private CountDownLatch initLatch = new CountDownLatch(1);
	
	public ClusteredSegmentManager(int numberOfSegments, Cluster cluster, LocalSegmentFactory localSegmentFactory, RemoteSegmentFactory remoteSegmentFactory) {
		this.numberOfSegments = numberOfSegments;
		this.cluster = cluster;
		this.cluster.getEventService().registerListener(this);
		this.segments = new LinkedList<Segment>();
		this.localSegmentFactory = localSegmentFactory;
		this.remoteSegmentFactory = remoteSegmentFactory;
		for(int i=0;i<numberOfSegments;i++) {
			Segment p = localSegmentFactory.createSegment(i);
			segments.add(p);
		}
	}

	public Segment findSegmentForActor(ActorId actor) {
		try {
			initLatch.await();
			int hash = hash(actor.getId());
			int partition = hash % numberOfSegments;
			readLock.lock();
			return segments.get(partition);
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new IllegalStateException("SegmentManager not initialized properly.");
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
			initLatch.countDown();
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
			logger.error("Exception while initializing new partitions.",e);
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
		RemoteSegment newSegment = remoteSegmentFactory.createSegment(i, member);
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
		LocalSegment newSegment = localSegmentFactory.createSegment(i);
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

}
