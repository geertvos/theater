package net.geertvos.theater.core.segmentation;

import org.apache.log4j.Logger;

import net.geertvos.gossip.api.cluster.ClusterMember;
import net.geertvos.theater.api.actors.ActorHandle;
import net.geertvos.theater.api.segmentation.Segment;
import net.geertvos.theater.core.networking.SegmentClient;

public class RemoteSegment implements Segment {

	private Logger log = Logger.getLogger(RemoteSegment.class);
	private final ClusterMember clusterMember;
	private final int id;
	private final SegmentClient client;
	private volatile boolean operational = false;
	
	public RemoteSegment(int id, ClusterMember clusterMember, SegmentClient client) {
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
	
	public ClusterMember getClusterMember() {
		return clusterMember;
	}

	public boolean isLocal() {
		return false;
	}

	public boolean isOperational() {
		return operational;
	}

	public void handleMessage(ActorHandle from, ActorHandle to, Object message) {
		log.info("RemoteSegment is handling message from "+from+" to "+to);
		client.sendMessage(from, to, message);
	}

}
