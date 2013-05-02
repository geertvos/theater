package net.geertvos.theater.core.partitioning;

import java.util.concurrent.atomic.AtomicBoolean;

import org.testng.log4testng.Logger;

import net.geertvos.gossip.api.cluster.ClusterMember;
import net.geertvos.theater.api.durability.PartitionMessageLog;
import net.geertvos.theater.api.messaging.Message;
import net.geertvos.theater.api.partitioning.Partition;
import net.geertvos.theater.core.durability.NoopPartitionMessageLog;
import net.geertvos.theater.core.networking.PartitionClient;

public class RemotePartition implements Partition {

	private Logger log = Logger.getLogger(RemotePartition.class);

	private final ClusterMember clusterMember;
	private final int id;
	private final int port;
	private PartitionClient client;
	private final PartitionMessageLog messageLog;
	private AtomicBoolean operational = new AtomicBoolean(false);
	
	public RemotePartition(int id, ClusterMember clusterMember, PartitionMessageLog messageLog) {
		this.id = id;
		this.clusterMember = clusterMember;
		this.port = Integer.parseInt(clusterMember.getMetaData("partitionServer.port"));
		this.messageLog = messageLog;
	}

	public RemotePartition(int id, ClusterMember clusterMember) {
		this(id,clusterMember,new NoopPartitionMessageLog());
	}

	public void handleMessage(Message message) {
		if(operational.get()) {
			log.debug("Sending message "+message+" to remote partition "+id);
			client.sendMessage(message);
		}
		messageLog.logMessage(message);
	}

	public int getId() {
		return id;
	}

	public void onInit() {
		client = new PartitionClient(clusterMember.getHost(), port);
		operational.set(true);
	}

	public void onDestroy() {
		client.disconnect();
		operational.set(true);
	}
	
	public ClusterMember getClusterMember() {
		return clusterMember;
	}

}
