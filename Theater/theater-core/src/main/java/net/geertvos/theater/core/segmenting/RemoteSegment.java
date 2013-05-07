package net.geertvos.theater.core.segmenting;

import java.util.UUID;

import net.geertvos.gossip.api.cluster.ClusterMember;
import net.geertvos.theater.api.durability.MessageLog;
import net.geertvos.theater.api.messaging.Message;
import net.geertvos.theater.api.partitioning.Segment;
import net.geertvos.theater.core.durability.NoopMessageLog;
import net.geertvos.theater.core.networking.SegmentClient;
import net.geertvos.theater.core.networking.SegmentMessage;

import org.testng.log4testng.Logger;

public class RemoteSegment implements Segment {

	private Logger log = Logger.getLogger(RemoteSegment.class);

	private final ClusterMember clusterMember;
	private final int id;
	private final int port;
	private SegmentClient client;
	private final MessageLog messageLog;
	private volatile boolean operational = false;
	private volatile boolean replayRequestSent = false;
	
	public RemoteSegment(int id, ClusterMember clusterMember, MessageLog messageLog) {
		this.id = id;
		this.clusterMember = clusterMember;
		this.port = Integer.parseInt(clusterMember.getMetaData("segmentServer.port"));
		this.messageLog = messageLog;
	}

	public RemoteSegment(int id, ClusterMember clusterMember) {
		this(id,clusterMember,new NoopMessageLog());
	}

	public void handleMessage(Message message) {
		if(operational) {
			log.debug("Sending message "+message+" to remote segment "+id);
			if(!replayRequestSent) {
				client.sendMessage(new SegmentMessage(1, UUID.randomUUID(), null, message.getTo()));
				replayRequestSent = true;
			}
			client.sendMessage(message);
		}
		messageLog.logMessage(message);
	}

	public int getId() {
		return id;
	}

	public void onInit() {
		client = new SegmentClient(clusterMember.getHost(), port);
		operational = true;
	}

	public void onDestroy() {
		if(operational) {
			client.disconnect();
		}
		operational = false;
		replayRequestSent = false;
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
