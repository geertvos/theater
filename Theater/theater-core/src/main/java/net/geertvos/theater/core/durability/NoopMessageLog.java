package net.geertvos.theater.core.durability;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.geertvos.theater.api.durability.MessageLog;
import net.geertvos.theater.api.messaging.Message;

public class NoopMessageLog implements MessageLog {

	private ConcurrentHashMap<UUID, Message> log = new ConcurrentHashMap<UUID,Message>();
	
	public void logMessage(Message message) {
		log.putIfAbsent(message.getMessageId(), message);
	}

	public void ackMessage(Message message) {
		log.remove(message);
	}

	public List<Message> getUnackedMessages() {
		List<Message> unacked = new ArrayList<Message>(log.values());
		Collections.sort(unacked, new TimeComperator());
		return unacked;
	}

	private class TimeComperator implements Comparator<Message> {

		public int compare(Message arg0, Message arg1) {
			Long t1 = arg0.getMessageId().timestamp();
			return t1.compareTo(arg1.getMessageId().timestamp());
		}
		
	}
	
}
