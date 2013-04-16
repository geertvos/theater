package net.geertvos.theater.core.partitioning;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.geertvos.gossip.api.cluster.Cluster;
import net.geertvos.gossip.api.cluster.ClusterEventListener;
import net.geertvos.gossip.api.cluster.ClusterMember;
import net.geertvos.theater.api.actors.ActorId;
import net.geertvos.theater.api.partitioning.Partition;
import net.geertvos.theater.api.partitioning.PartitionManager;

import org.apache.log4j.Logger;

public class ClusteredPartitionManager implements PartitionManager, ClusterEventListener {

	private Logger logger = Logger.getLogger(ClusteredPartitionManager.class);
	private final Cluster cluster;
	private final LocalPartitionFactory localPartitionFactory;
	private final RemotePartitionFactory remotePartitionFactory;
	
	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	private final Lock readLock = lock.readLock();
	private final Lock writeLock = lock.writeLock();
	
	private int numberOfPartitions = 512;
	private final List<Partition> partitions;
	
	public ClusteredPartitionManager(int numberOfPartitions, Cluster cluster, LocalPartitionFactory localPartitionFactory, RemotePartitionFactory remotePartitionFactory) {
		this.numberOfPartitions = numberOfPartitions;
		this.cluster = cluster;
		this.cluster.getEventService().registerListener(this);
		this.partitions = new LinkedList<Partition>();
		this.localPartitionFactory = localPartitionFactory;
		this.remotePartitionFactory = remotePartitionFactory;
		for(int i=0;i<numberOfPartitions;i++) {
			partitions.add(localPartitionFactory.createPartition(i));
		}
	}

	public Partition findPartitionForActor(ActorId actor) {
		try {
			int hash = hash(actor.getId());
			int partition = hash % numberOfPartitions;
			readLock.lock();
			return partitions.get(partition);
		} finally {
			readLock.unlock();
		}
	}
	

	public void onNewActiveMember(ClusterMember member) {
		// TODO Auto-generated method stub
		
	}

	public void onNewInactiveMember(ClusterMember member) {
		// TODO Auto-generated method stub
		
	}

	public void onMemberActivated(ClusterMember member) {
		// TODO Auto-generated method stub
		
	}

	public void onMemberDeactivated(ClusterMember member) {
		// TODO Auto-generated method stub
		
	}

	public void onClusterStabilized(List<ClusterMember> members) {
		logger.info("New cluster members:");
		for(ClusterMember member : members) {
			logger.info("member: "+member.getId());
		}
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
		} finally {
			writeLock.unlock();
		}
	}
	
	private void createRemotePartition(int i, ClusterMember member) {
		Partition current = partitions.get(i);
		if(current instanceof RemotePartition && ((RemotePartition)current).getClusterMember().getId().equals(member.getId())) {
			//Partition stays
			logger.info("Partition "+i+" stays a remote partition on cluster member "+member.getId());
			return;
		}
		logger.info("Partition "+i+" relocates to remote cluster member "+member.getId());
		current.onDestroy();
		RemotePartition newPartition = remotePartitionFactory.createPartition(i, member);
		partitions.remove(i);
		partitions.add(i, newPartition);
		newPartition.onInit();
	}

	private void createLocalPartition(int i) {
		Partition current = partitions.get(i);
		if(current instanceof LocalPartition) {
			//Partition stays
			logger.info("Partition "+i+" stays at local cluster member");
			return;
		}
		logger.info("Partition "+i+" relocates to local cluster member");
		current.onDestroy();
		LocalPartition newPartition = localPartitionFactory.createPartition(i);
		partitions.remove(i);
		partitions.add(i, newPartition);
		newPartition.onInit();
		
	}

	private int hash(int number) {
		return number;
	}
	
	private int hash(UUID id) {
		long idLong = id.timestamp();
		return (int) Math.round(idLong * 31 / 3.31);
	}


	public void onClusterDestabilized() {
		// TODO Auto-generated method stub
		
	}

}
