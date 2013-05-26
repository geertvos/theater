package net.geertvos.theater.cassandra.actorstore;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.geertvos.theater.api.actors.ActorId;
import net.geertvos.theater.api.actorstore.ActorStateStore;

public class CassandraActorStore implements ActorStateStore {

	private CassandraActorDao dao;
	private Map<ActorId,Object> stateCache = new ConcurrentHashMap<ActorId, Object>();
	
	public CassandraActorStore(CassandraActorDao dao) {
		this.dao = dao;
	}
	
	public Object readActorState(int partition, ActorId actorId) {
		Object state = stateCache.get(actorId);
		if(state != null) {
			return state;
		}
		state = dao.read(partition, actorId.getId());
		return state;
	}

	public void writeActorState(int partition, ActorId actorId,	Object actorState) {
		if(actorState != null) {
			dao.write(partition, actorId, actorState);
			stateCache.put(actorId, actorState);
		}
	}

}
