package net.geertvos.theater.api.factory;

import net.geertvos.theater.api.actors.Actor;
import net.geertvos.theater.api.messaging.Message;

public interface ActorFactory {

	Actor createActor(Message input);
	
}
