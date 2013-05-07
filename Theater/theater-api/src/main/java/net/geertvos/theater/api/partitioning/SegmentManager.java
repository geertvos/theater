package net.geertvos.theater.api.partitioning;

import net.geertvos.theater.api.actors.ActorId;

public interface SegmentManager {

	Segment findSegmentForActor(ActorId actor);
	
}
