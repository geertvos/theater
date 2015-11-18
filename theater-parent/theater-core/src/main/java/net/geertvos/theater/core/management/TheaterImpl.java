package net.geertvos.theater.core.management;

import java.util.HashMap;
import java.util.Map;

import net.geertvos.theater.api.management.Theater;
import net.geertvos.theater.api.actors.ActorHandle;
import net.geertvos.theater.api.management.ActorSystem;

public class TheaterImpl implements Theater {

	private final Map<String, ActorSystem> systems = new HashMap<String, ActorSystem>();
	
	public ActorSystem getActorSystem(String id) {
		return systems.get(id);
	}

	public void registerActorSystem(String id, ActorSystem system) {
		systems.put(id, system);
	}
	
	public void sendMessage(ActorHandle from, ActorHandle to, Object message) {
		getActorSystem(to.getSystem()).handleMessage(from, to, message);
	}
}