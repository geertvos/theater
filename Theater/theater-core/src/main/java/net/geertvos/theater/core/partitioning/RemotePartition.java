package net.geertvos.theater.core.partitioning;

import net.geertvos.gossip.api.cluster.ClusterMember;
import net.geertvos.theater.api.messaging.Message;
import net.geertvos.theater.api.partitioning.Partition;

public class RemotePartition implements Partition {

	private final ClusterMember clusterMember;
	private final int id;

	public RemotePartition(int id, ClusterMember clusterMember) {
		this.id = id;
		this.clusterMember = clusterMember;
	}
	
	public void handleMessage(Message message) {
		// TODO Auto-generated method stub
		
	}

	public int getId() {
		return id;
	}

	public void onInit() {
		// TODO Auto-generated method stub
		
	}

	public void onDestroy() {
		// TODO Auto-generated method stub
		
	}
	
	public ClusterMember getClusterMember() {
		return clusterMember;
	}

}
