package net.geertvos.theater.core.networking;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.geertvos.theater.api.actors.ActorId;
import net.geertvos.theater.api.messaging.Message;

public class PartitionMessage implements Message {

	private int type;
	private UUID messageId;
	private ActorId to;
	private ActorId from;
	private Map<String, String> parameters = new HashMap<String, String>();

	public PartitionMessage(int type, UUID messageId, ActorId from, ActorId to) {
		this.messageId = messageId;
		this.from = from;
		this.to = to;
		this.type = type;
	}

	public PartitionMessage() {
	}

	public void setTo(ActorId id) {
		this.to = id;
	}
	
	public void setFrom(ActorId from) {
		this.from = from;
	}
	
	public ActorId getTo() {
		return to;
	}

	public ActorId getFrom() {
		return from;
	}
	
	public void setType(int type) {
		this.type = type;
	}
	
	public int getType() {
		return type;
	}

	public Map<String, String> getAllParameters() {
		return parameters;
	}

	public void setParameter(String name, String value) {
		parameters.put(name, name);
	}

	public String getParameter(String name) {
		return parameters.get(name);
	}

	public void setMessageId(UUID id) {
		this.messageId = id;
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Message ").append(messageId).append(" : [").append(from.getId()).append("] -> [").append(to.getId()).append("] : ").append(parameters);
		return builder.toString();
	}

	public UUID getMessageId() {
		return messageId;
	}
}
