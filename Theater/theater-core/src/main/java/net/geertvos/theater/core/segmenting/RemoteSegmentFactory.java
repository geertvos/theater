package net.geertvos.theater.core.segmenting;

import net.geertvos.gossip.api.cluster.ClusterMember;

public interface RemoteSegmentFactory {

	RemoteSegment createSegment(int id, ClusterMember host);
	
}
