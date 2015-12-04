package net.geertvos.theater.core.networking;

import net.geertvos.theater.api.clustering.GroupMember;

public interface SegmentClientFactory {

	SegmentClient createClient(int segment, GroupMember member);

	SegmentClient createClient(GroupMember member);
	
}
