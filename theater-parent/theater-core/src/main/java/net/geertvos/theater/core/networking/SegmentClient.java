package net.geertvos.theater.core.networking;

import net.geertvos.theater.api.messaging.Message;

public interface SegmentClient {

	void start();
	
	void sendMessage(final Message message);
	
	void stop();
	
}
