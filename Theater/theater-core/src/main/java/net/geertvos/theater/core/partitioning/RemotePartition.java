package net.geertvos.theater.core.partitioning;

import net.geertvos.gossip.api.cluster.ClusterMember;
import net.geertvos.theater.api.messaging.Message;
import net.geertvos.theater.api.partitioning.Partition;
import net.geertvos.theater.core.networking.PartitionClient;

public class RemotePartition implements Partition {

	private final ClusterMember clusterMember;
	private final int id;
	private final int port;
	private PartitionClient client;
	
	public RemotePartition(int id, ClusterMember clusterMember) {
		this.id = id;
		this.clusterMember = clusterMember;
		this.port = Integer.parseInt(clusterMember.getMetaData("partitionServer.port"));
	}
	
	public void handleMessage(Message message) {
		client.sendMessage(message);
	}

	public int getId() {
		return id;
	}

	public void onInit() {
		client = new PartitionClient(clusterMember.getHost(), port);
	}

	public void onDestroy() {
		client.disconnect();
	}
	
	public ClusterMember getClusterMember() {
		return clusterMember;
	}

}
