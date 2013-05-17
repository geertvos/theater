package net.geertvos.theater.core.messaging;

import net.geertvos.theater.api.actors.ActorId;
import net.geertvos.theater.api.messaging.Message;
import net.geertvos.theater.api.messaging.MessageSender;
import net.geertvos.theater.api.segmentation.Segment;
import net.geertvos.theater.api.segmentation.SegmentManager;

public class SegmentMessageSender implements MessageSender {

	private SegmentManager manager;

	public SegmentMessageSender(SegmentManager manager) {
		this.manager = manager;
	}
	
	public void sendMessage(Message message) {
		ActorId id = message.getTo();
		Segment segment = manager.findSegmentForActor(id);
		segment.handleMessage(message);
	}

}
