package net.geertvos.theater.api.messaging;

import java.util.UUID;

import net.geertvos.theater.api.actors.ActorHandle;

public interface Message {

	UUID getMessageId();
	
	ActorHandle getTo();
	
	ActorHandle getFrom();
	
	void setParameter(String name, String value);
	
	String getParameter(String name);
	
	int getType();
	
	boolean isDurable();
}
