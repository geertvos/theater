package net.geertvos.theater.core.networking;

import net.geertvos.theater.api.actors.ActorHandle;

public interface SegmentClient {

	void start();
	
	void sendMessage(final ActorHandle from, final ActorHandle to, final Object message);
	
	void stop();
	
}
