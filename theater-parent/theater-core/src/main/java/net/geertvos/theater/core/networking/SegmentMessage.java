package net.geertvos.theater.core.networking;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.geertvos.theater.api.actors.ActorHandle;
import net.geertvos.theater.api.messaging.Message;

public class SegmentMessage implements Message {

	private int type;
	private UUID messageId;
	private ActorHandle to;
	private ActorHandle from;
	private Map<String, String> parameters = new HashMap<String, String>();
	private boolean durable = true;

	public SegmentMessage(int type, UUID messageId, ActorHandle from, ActorHandle to) {
		this.messageId = messageId;
		this.from = from;
		this.to = to;
		this.type = type;
	}

	public SegmentMessage() {
	}

	public void setTo(ActorHandle id) {
		this.to = id;
	}
	
	public void setFrom(ActorHandle from) {
		this.from = from;
	}
	
	public ActorHandle getTo() {
		return to;
	}

	public ActorHandle getFrom() {
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

	public boolean isDurable() {
		return durable;
	}

	public void setDurable(boolean durable) {
		this.durable = durable;
	}
}
