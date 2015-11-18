package net.geertvos.theater.api.messaging;

import net.geertvos.theater.api.actors.ActorHandle;

public interface MessageSender {

	void sendMessage(ActorHandle from, ActorHandle to, Object message);
	
}
