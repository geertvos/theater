package net.geertvos.theater.core.durability;

import java.util.Collections;
import java.util.List;

import net.geertvos.theater.api.durability.MessageLog;
import net.geertvos.theater.api.messaging.Message;

public class NoopMessageLog implements MessageLog {

	public void logMessage(Message message) {
	}

	public void ackMessage(Message message) {
	}

	public List<Message> getUnackedMessages() {
		return Collections.emptyList();
	}

}
