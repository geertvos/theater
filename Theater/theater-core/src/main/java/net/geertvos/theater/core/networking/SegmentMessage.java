package net.geertvos.theater.core.networking;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.geertvos.theater.api.actors.ActorId;
import net.geertvos.theater.api.messaging.Message;

public class SegmentMessage implements Message {

	private int type;
	private UUID messageId;
	private ActorId to;
	private ActorId from;
	private Map<String, String> parameters = new HashMap<String, String>();

	public SegmentMessage(int type, UUID messageId, ActorId from, ActorId to) {
		this.messageId = messageId;
		this.from = from;
		this.to = to;
		this.type = type;
	}

	public SegmentMessage() {
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
		parameters.put(name, value);
	}

	public String getParameter(String name) {
		return parameters.get(name);
	}

	public void setMessageId(UUID id) {
		this.messageId = id;
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Message ").append(messageId).append(" : [");
		if(from != null) {
			builder.append(from.getId()).append("] -> [");
		}
		if(to != null) {
			builder.append(to.getId());
		}
		builder.append("] : ").append(parameters);
		return builder.toString();
	}

	public UUID getMessageId() {
		return messageId;
	}
}
