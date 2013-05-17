package net.geertvos.theater.api.messaging;

import java.util.UUID;

import net.geertvos.theater.api.actors.ActorId;

public interface Message {

	UUID getMessageId();
	
	ActorId getTo();
	
	ActorId getFrom();
	
	void setParameter(String name, String value);
	
	String getParameter(String name);
	
	int getType();
	
	boolean isDurable();
}
