package net.geertvos.theater.core.partitioning;

import net.geertvos.gossip.api.cluster.ClusterMember;

public interface RemotePartitionFactory {

	RemotePartition createPartition(int id, ClusterMember host);
	
}
