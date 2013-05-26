package net.geertvos.theater.api.messaging;

import net.geertvos.theater.api.actors.ActorId;

public interface MessageSender {

	void sendMessage(ActorId from, ActorId to, Object message);
	
}
