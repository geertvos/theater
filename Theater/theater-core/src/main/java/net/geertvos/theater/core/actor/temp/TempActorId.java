package net.geertvos.theater.core.actor.temp;

import java.util.UUID;

import net.geertvos.theater.core.actor.ActorIdImpl;

public class TempActorId extends ActorIdImpl {

	private String clusterMemberId;
	
	public TempActorId(String cluster, String system, String type, UUID id, String memberId) {
		super(cluster,system,type,id);
		this.clusterMemberId = memberId;
	}
	
	public TempActorId() {
	}

	
	public void setMemberId(String memberId) {
		this.clusterMemberId = memberId;
	}
	
	public String getMemberId() {
		return clusterMemberId;
	}
	
}
