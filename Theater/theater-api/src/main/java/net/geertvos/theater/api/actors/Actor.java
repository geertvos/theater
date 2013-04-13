package net.geertvos.theater.api.actors;

import net.geertvos.theater.api.messaging.Message;

public interface Actor {

	ActorId getId();
	
	void handleMessage(Message message);
	
}
