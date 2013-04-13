package net.geertvos.theater.api.partitioning;

import net.geertvos.theater.api.actors.ActorId;

public interface PartitionManager {

	Partition findPartitionForActor(ActorId actor);
	
}
