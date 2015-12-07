package net.geertvos.theater.api.management;

import java.util.UUID;

import net.geertvos.theater.api.actors.ActorHandle;

public interface Theater {

	public void sendMessage(ActorHandle from, ActorHandle to, Object message);

	public void sendMessage(ActorHandle to, Object message);
	
	public ActorHandle getTempActor(Class<?> type, UUID id);
	
	public ActorHandle getActor(Class<?> type, UUID id);
	
	public ActorHandle getServiceActor(Class<?> type, String service);
	
}
