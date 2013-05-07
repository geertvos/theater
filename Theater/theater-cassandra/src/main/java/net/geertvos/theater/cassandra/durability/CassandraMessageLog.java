package net.geertvos.theater.cassandra.durability;

import java.util.List;

import net.geertvos.theater.api.durability.MessageLog;
import net.geertvos.theater.api.messaging.Message;

public class CassandraMessageLog implements MessageLog {

	private final int segment;
	private final CassandraMessageLogDao dao;
	
	public CassandraMessageLog(int segment, CassandraMessageLogDao dao) {
		this.segment = segment;
		this.dao = dao;
	}

	public void logMessage(Message message) {
		dao.write(segment, message);
	}

	public void ackMessage(Message message) {
		dao.delete(segment, message.getMessageId());
	}

	public List<Message> getUnackedMessages() {
		return dao.getSegment(segment);
	}

}
