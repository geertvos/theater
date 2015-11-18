package net.geertvos.theater.api.segmentation;

import net.geertvos.theater.api.actors.ActorHandle;

public interface SegmentManager {

	Segment findSegmentForActor(ActorHandle actor);
	
}
