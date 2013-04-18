package net.geertvos.theater.core.messaging;

import net.geertvos.theater.api.actors.ActorId;
import net.geertvos.theater.api.messaging.Message;
import net.geertvos.theater.api.messaging.MessageSender;
import net.geertvos.theater.api.partitioning.Partition;
import net.geertvos.theater.api.partitioning.PartitionManager;

public class PartitionMessageSender implements MessageSender {

	private PartitionManager manager;

	public PartitionMessageSender(PartitionManager manager) {
		this.manager = manager;
	}
	
	public void sendMessage(Message message) {
		ActorId id = message.getTo();
		Partition partition = manager.findPartitionForActor(id);
		partition.handleMessage(message);
	}

}
