package net.geertvos.theater.core.management;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.geertvos.theater.api.actors.ActorHandle;
import net.geertvos.theater.api.clustering.GroupMembershipProvider;
import net.geertvos.theater.api.management.ActorSystem;
import net.geertvos.theater.api.management.Theater;
import net.geertvos.theater.core.actor.ActorHandleImpl;
import net.geertvos.theater.core.actor.temp.TempActorHandle;

public class TheaterImpl implements Theater {

	private final Map<String, ActorSystem> systems = new HashMap<String, ActorSystem>();
	private GroupMembershipProvider cluster;

	public TheaterImpl(GroupMembershipProvider cluster) {
		this.cluster = cluster;
	}
	
	ActorSystem getActorSystem(String id) {
		return systems.get(id);
	}

	void registerActorSystem(String id, ActorSystem system) {
		systems.put(id, system);
	}
	
	public void sendMessage(ActorHandle from, ActorHandle to, Object message) {
		getActorSystem(to.getSystem()).handleMessage(from, to, message);
	}
	
	public ActorHandle getTempActor(Class type, UUID id) {
		return new TempActorHandle("Not in use yet", "temp", type.getName(), id, cluster.getLocalMember().getId());
	}
	
	public ActorHandle getActor(Class type, UUID id) {
		return new ActorHandleImpl("cluster not in use", "segmented", type.getName(), id);
	}
	
	public ActorHandle getServiceActor(Class type, String service) {
		throw new UnsupportedOperationException();
	}
}
