package net.geertvos.theater.cassandra.durability;

import java.util.List;

import net.geertvos.theater.api.durability.MessageLog;
import net.geertvos.theater.api.messaging.Message;

public class CassandraMessageLog implements MessageLog {

	private final int partition;
	private final CassandraMessageLogDao dao;
	
	public CassandraMessageLog(int partition, CassandraMessageLogDao dao) {
		this.partition = partition;
		this.dao = dao;
	}

	public void logMessage(Message message) {
		dao.write(partition, message);
	}

	public void ackMessage(Message message) {
		dao.delete(partition, message.getMessageId());
	}

	public List<Message> getUnackedMessages() {
		return dao.getPartition(partition);
	}

}
