package net.geertvos.theater.api.actors;

import java.util.UUID;

public interface ActorHandle {

	UUID getId();
	
	String getType();
	
	String getSystem();
	
	String getCluster();
	
}
