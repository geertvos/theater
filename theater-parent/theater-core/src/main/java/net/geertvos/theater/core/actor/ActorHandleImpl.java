package net.geertvos.theater.core.actor;

import java.util.UUID;

import net.geertvos.theater.api.actors.ActorHandle;

public class ActorHandleImpl implements ActorHandle {

	private static final String format = "theater/%s/%s/%s/%s";
	
	private UUID id;
	private String cluster;
	private String type;
	private String system;

	public ActorHandleImpl(String cluster, String system, String type, UUID id) {
		this.id = id;
		this.cluster = cluster;
		this.system = system;
		this.type = type;
	}
	
	public ActorHandleImpl() {
	}
	
	public UUID getId() {
		return id;
	}

	public String getCluster() {
		return cluster;
	}

	public String toString() {
		return String.format(format, cluster, system, type, id.toString());
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public void setCluster(String cluster) {
		this.cluster = cluster;
	}

	public String getType() {
		return this.type;
	}

	public String getSystem() {
		return this.system;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public void setSystem(String system) {
		this.system = system;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cluster == null) ? 0 : cluster.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((system == null) ? 0 : system.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		ActorHandleImpl other = (ActorHandleImpl) obj;
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
		if (system == null) {
			if (other.system != null)
				return false;
		} else if (!system.equals(other.system))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}
	
}
