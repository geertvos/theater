package net.geertvos.theater.core.actor.temp;

import java.util.UUID;

import net.geertvos.theater.core.actor.ActorHandleImpl;

public class TempActorHandle extends ActorHandleImpl {

	private String clusterMemberId;
	
	public TempActorHandle(String cluster, String system, String type, UUID id, String memberId) {
		super(cluster,system,type,id);
		this.clusterMemberId = memberId;
	}
	
	public TempActorHandle() {
	}

	
	public void setMemberId(String memberId) {
		this.clusterMemberId = memberId;
	}
	
	public String getMemberId() {
		return clusterMemberId;
	}
	
}
