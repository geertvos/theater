package net.geertvos.theater.core.segmentation;

import net.geertvos.theater.api.actors.ActorHandle;
import net.geertvos.theater.api.clustering.GroupMember;
import net.geertvos.theater.api.segmentation.Segment;
import net.geertvos.theater.core.networking.SegmentClient;

import org.apache.log4j.Logger;

public class RemoteSegment implements Segment {

	private static final Logger LOG = Logger.getLogger(RemoteSegment.class);
	private final GroupMember clusterMember;
	private final int id;
	private final SegmentClient client;
	private volatile boolean operational = false;
	
	public RemoteSegment(int id, GroupMember clusterMember, SegmentClient client) {
		this.id = id;
		this.clusterMember = clusterMember;
		this.client = client;
	}

	public int getId() {
		return id;
	}

	public void onInit() {
		operational = true;
		client.start();
	}

	public void onDestroy() {
		operational = false;
	}
	
	public GroupMember getClusterMember() {
		return clusterMember;
	}

	public boolean isLocal() {
		return false;
	}

	public boolean isOperational() {
		return operational;
	}

	public void handleMessage(ActorHandle from, ActorHandle to, Object message) {
		LOG.info("RemoteSegment is handling message from "+from+" to "+to);
		client.sendMessage(from, to, message);
	}

}
