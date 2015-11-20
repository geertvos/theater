package net.geertvos.theater.api.management;

import net.geertvos.theater.api.actors.ActorHandle;

public interface ActorSystem {

	void handleMessage(ActorHandle from, ActorHandle to, Object message);
	
}
