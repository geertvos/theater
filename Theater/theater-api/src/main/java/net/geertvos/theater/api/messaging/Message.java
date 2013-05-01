package net.geertvos.theater.api.messaging;

import net.geertvos.theater.api.actors.ActorId;

public interface Message {

	long getSequenceNumber();
	
	ActorId getTo();
	
	ActorId getFrom();
	
	void setParameter(String name, String value);
	
	String getParameter(String name);
	
}
