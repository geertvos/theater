package net.geertvos.theater.core.partitioning;

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
import net.geertvos.theater.api.partitioning.Partition;
import net.geertvos.theater.api.partitioning.PartitionManager;
import net.geertvos.theater.core.hashing.FakeHashFunction;

import org.apache.log4j.Logger;

import com.esotericsoftware.minlog.Log;

public class ClusteredPartitionManager implements PartitionManager, ClusterEventListener {

	private Logger logger = Logger.getLogger(ClusteredPartitionManager.class);
	
	private final Cluster cluster;
	private final LocalPartitionFactory localPartitionFactory;
	private final RemotePartitionFactory remotePartitionFactory;
	
	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	private final Lock readLock = lock.readLock();
	private final Lock writeLock = lock.writeLock();
	
	private final int numberOfPartitions;
	private final List<Partition> partitions;
	private ConsistentHashFunction hashFunction =  new FakeHashFunction();
	
	private CountDownLatch initLatch = new CountDownLatch(1);
	
	public ClusteredPartitionManager(int numberOfPartitions, Cluster cluster, LocalPartitionFactory localPartitionFactory, RemotePartitionFactory remotePartitionFactory) {
		this.numberOfPartitions = numberOfPartitions;
		this.cluster = cluster;
		this.cluster.getEventService().registerListener(this);
		this.partitions = new LinkedList<Partition>();
		this.localPartitionFactory = localPartitionFactory;
		this.remotePartitionFactory = remotePartitionFactory;
		for(int i=0;i<numberOfPartitions;i++) {
			Partition p = localPartitionFactory.createPartition(i);
			partitions.add(p);
		}
	}

	public Partition findPartitionForActor(ActorId actor) {
		try {
			initLatch.await();
			int hash = hash(actor.getId());
			int partition = hash % numberOfPartitions;
			readLock.lock();
			return partitions.get(partition);
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new IllegalStateException("PartitionManager not initialized properly.");
		} finally {
			readLock.unlock();
		}
	}
	

	public void onNewActiveMember(ClusterMember member, List<ClusterMember> members) {
		Log.info("Cluster member "+member.getId()+" joined, redistributing partitions.");
		updatePartitions(members);
	}

	public void onNewInactiveMember(ClusterMember member, List<ClusterMember> members) {
	}

	public void onMemberActivated(ClusterMember member, List<ClusterMember> members) {
		Log.info("Cluster member "+member.getId()+" joined, redistributing partitions.");
		updatePartitions(members);
	}

	public void onMemberDeactivated(ClusterMember member, List<ClusterMember> members) {
		Log.info("Cluster member "+member.getId()+" left, redistributing partitions.");
		updatePartitions(members);
	}

	private void updatePartitions(List<ClusterMember> members) {
		try {
			writeLock.lock();
			ClusterMember me = cluster.getLocalMember();
			int memberCount = members.size();
			for(int i=0;i<numberOfPartitions;i++) {
				int memberNumber =  hash(i) % memberCount;
				ClusterMember member = members.get(memberNumber);
				if(member.getId().equals(me.getId())) {
					createLocalPartition(i);
				} else {
					createRemotePartition(i, member);
				}
			}
			Log.info("Partitions distributed, awaiting cluster stability.");
		} catch(Exception e) {
			Log.error("Error while updating partition distribution",e);
		} finally {
			initLatch.countDown();
			writeLock.unlock();
		}
	}
	
	public void onClusterStabilized(List<ClusterMember> members) {
		try {
			readLock.lock();
			for(Partition p : partitions) {
				if(!p.isOperational()) {
					Log.debug("Partition "+p.getId()+" initialized.");
					p.onInit();
				}
			}
		} catch(Exception e) {
			logger.error("Exception while initializing new partitions.",e);
		} finally {
			readLock.unlock();
		}
		
	}
	
	private void createRemotePartition(int i, ClusterMember member) {
		Partition current = partitions.get(i);
		if(current instanceof RemotePartition && ((RemotePartition)current).getClusterMember().getId().equals(member.getId())) {
			logger.info("Partition "+i+" stays a remote partition on cluster member "+member.getId());
			return;
		}
		logger.info("Partition "+i+" relocates to remote cluster member "+member.getId());
		current.onDestroy();
		logger.debug("Partition "+i+" destroyed.");
		RemotePartition newPartition = remotePartitionFactory.createPartition(i, member);
		partitions.remove(i);
		partitions.add(i, newPartition);
	}

	private void createLocalPartition(int i) {
		Partition current = partitions.get(i);
		if(current.isLocal()) {
			logger.info("Partition "+i+" stays at local cluster member");
			return;
		}
		logger.info("Partition "+i+" relocates to local cluster member");
		current.onDestroy();
		logger.debug("Partition "+i+" destroyed.");
		LocalPartition newPartition = localPartitionFactory.createPartition(i);
		partitions.remove(i);
		partitions.add(i, newPartition);
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
