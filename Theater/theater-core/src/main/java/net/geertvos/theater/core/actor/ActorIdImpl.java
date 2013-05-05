package net.geertvos.theater.core.actor;

import java.util.UUID;

import org.codehaus.jackson.annotate.JsonProperty;

import net.geertvos.theater.api.actors.ActorId;

public class ActorIdImpl implements ActorId {

	private UUID id;
	private String cluster;

	public ActorIdImpl(@JsonProperty("id")UUID id, @JsonProperty("cluster") String cluster) {
		this.id = id;
		this.cluster = cluster;
	}
	
	public ActorIdImpl() {
	}
	
	public UUID getId() {
		return id;
	}

	public String getCluster() {
		return cluster;
	}

	public String toString() {
		return id+"@"+cluster;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cluster == null) ? 0 : cluster.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ActorIdImpl other = (ActorIdImpl) obj;
		if (cluster == null) {
			if (other.cluster != null)
				return false;
		} else if (!cluster.equals(other.cluster))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public void setCluster(String cluster) {
		this.cluster = cluster;
	}
	
}
