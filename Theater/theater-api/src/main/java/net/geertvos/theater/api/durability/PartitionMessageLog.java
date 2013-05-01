package net.geertvos.theater.api.durability;

import java.util.List;

import net.geertvos.theater.api.messaging.Message;

public interface PartitionMessageLog {

	void logMessage(Message message);
	
	void ackMessage(Message message);
	
	List<Message> getUnackedMessages();
	
}
