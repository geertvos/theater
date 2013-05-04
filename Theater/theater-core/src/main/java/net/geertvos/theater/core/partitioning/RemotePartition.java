package net.geertvos.theater.core.partitioning;

import java.util.concurrent.atomic.AtomicBoolean;

import org.testng.log4testng.Logger;

import net.geertvos.gossip.api.cluster.ClusterMember;
import net.geertvos.theater.api.durability.MessageLog;
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
	private final MessageLog messageLog;
	private volatile boolean operational = false;
	
	public RemotePartition(int id, ClusterMember clusterMember, MessageLog messageLog) {
		this.id = id;
		this.clusterMember = clusterMember;
		this.port = Integer.parseInt(clusterMember.getMetaData("partitionServer.port"));
		this.messageLog = messageLog;
	}

	public RemotePartition(int id, ClusterMember clusterMember) {
		this(id,clusterMember,new NoopPartitionMessageLog());
	}

	public void handleMessage(Message message) {
		if(operational) {
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
		operational = true;
	}

	public void onDestroy() {
		if(operational) {
			client.disconnect();
		}
		operational = false;
	}
	
	public ClusterMember getClusterMember() {
		return clusterMember;
	}

	public boolean isLocal() {
		return false;
	}

	public boolean isOperational() {
		return operational;
	}

}
