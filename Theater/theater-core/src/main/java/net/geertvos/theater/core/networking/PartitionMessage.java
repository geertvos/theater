package net.geertvos.theater.core.networking;

import java.util.HashMap;
import java.util.Map;

import net.geertvos.theater.api.actors.ActorId;
import net.geertvos.theater.api.messaging.Message;

import org.codehaus.jackson.annotate.JsonAnyGetter;
import org.codehaus.jackson.annotate.JsonAnySetter;
import org.codehaus.jackson.annotate.JsonTypeInfo;

public class PartitionMessage implements Message {

	private ActorId to;
	private ActorId from;
	private Map<String, String> parameters = new HashMap<String, String>();

	public PartitionMessage(ActorId from, ActorId to) {
		this.from = from;
		this.to = to;
	}

	public PartitionMessage() {
	}

	public void setTo(ActorId id) {
		this.to = id;
	}
	
	public void setFrom(ActorId from) {
		this.from = from;
	}
	
	@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
	public ActorId getTo() {
		return to;
	}

	@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
	public ActorId getFrom() {
		return from;
	}

	@JsonAnyGetter
	public Map<String, String> getAllParameters() {
		return parameters;
	}

	@JsonAnySetter
	public void setParameter(String name, String value) {
		parameters.put(name, name);
	}

	public String getParameter(String name) {
		return parameters.get(name);
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Message: [").append(from.getId()).append("] -> [").append(to.getId()).append("] : ").append(parameters);
		return builder.toString();
	}
}
