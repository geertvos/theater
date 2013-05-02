package net.geertvos.theater.core.durability;

import java.util.Collections;
import java.util.List;

import net.geertvos.theater.api.durability.PartitionMessageLog;
import net.geertvos.theater.api.messaging.Message;

public class NoopPartitionMessageLog implements PartitionMessageLog {

	public void logMessage(Message message) {
	}

	public void ackMessage(Message message) {
	}

	public List<Message> getUnackedMessages() {
		return Collections.EMPTY_LIST;
	}

}
