package net.geertvos.theater.core.actor;

import java.util.UUID;

import net.geertvos.theater.api.actors.ActorId;

public class ActorIdImpl implements ActorId {

	private final UUID id;
	private final String cluster;

	public ActorIdImpl(UUID id, String cluster) {
		this.id = id;
		this.cluster = cluster;
	}
	
	public UUID getId() {
		return id;
	}

	public String getCluster() {
		return cluster;
	}

}
