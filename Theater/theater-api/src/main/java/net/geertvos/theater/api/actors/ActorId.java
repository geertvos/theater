package net.geertvos.theater.api.actors;

import java.util.UUID;

public interface ActorId {

	UUID getId();
	
	String getCluster();
	
}
