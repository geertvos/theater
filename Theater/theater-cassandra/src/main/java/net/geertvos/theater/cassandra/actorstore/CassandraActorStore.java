package net.geertvos.theater.cassandra.actorstore;

import net.geertvos.theater.api.actors.Actor;
import net.geertvos.theater.api.actors.ActorId;
import net.geertvos.theater.api.actorstore.ActorStore;

public class CassandraActorStore implements ActorStore {

	private CassandraActorDao dao;
	
	public CassandraActorStore(CassandraActorDao dao) {
		this.dao = dao;
	}
	
	public void writeActor(int partition, Actor actor) {
		dao.write(partition, actor);
	}

	public Actor readActor(int partition, ActorId actorId) {
		return dao.read(partition, actorId.getId());
	}

}
