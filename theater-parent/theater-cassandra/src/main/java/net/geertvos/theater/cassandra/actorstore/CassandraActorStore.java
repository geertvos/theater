package net.geertvos.theater.cassandra.actorstore;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.geertvos.theater.api.actors.ActorHandle;
import net.geertvos.theater.api.actorstore.ActorStateStore;

public class CassandraActorStore implements ActorStateStore {

	private CassandraActorDao dao;
	private Map<ActorHandle,Object> stateCache = new ConcurrentHashMap<ActorHandle, Object>();
	
	public CassandraActorStore(CassandraActorDao dao) {
		this.dao = dao;
	}
	
	public Object readActorState(ActorHandle handle) {
		Object state = stateCache.get(handle);
		if(state != null) {
			return state;
		}
		state = dao.read(handle.getId());
		return state;
	}

	public void writeActorState(ActorHandle handle,	Object actorState) {
		if(actorState != null) {
			dao.write(handle, actorState);
			stateCache.put(handle, actorState);
		}
	}

}
