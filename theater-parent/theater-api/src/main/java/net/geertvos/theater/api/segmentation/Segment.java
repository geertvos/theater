package net.geertvos.theater.api.segmentation;

import net.geertvos.theater.api.actors.ActorHandle;

public interface Segment {

	boolean isLocal();
	
	boolean isOperational();
	
	void onInit();
	
	void onDestroy();
	
	void handleMessage(ActorHandle from, ActorHandle to, Object message);
	
	int getId();
}
