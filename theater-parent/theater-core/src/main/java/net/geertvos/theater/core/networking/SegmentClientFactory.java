package net.geertvos.theater.core.networking;

import net.geertvos.gossip.api.cluster.ClusterMember;

public interface SegmentClientFactory {

	SegmentClient createClient(int segment, ClusterMember member);

	SegmentClient createClient(ClusterMember member);
	
}
