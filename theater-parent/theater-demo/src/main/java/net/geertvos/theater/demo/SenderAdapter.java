package net.geertvos.theater.demo;

import net.geertvos.theater.api.actors.ActorHandle;
import net.geertvos.theater.api.messaging.MessageSender;

public class SenderAdapter implements MessageSender {

	private MessageSender sender;

	public void setMessageSender(MessageSender sender) {
		this.sender = sender;
	}
	
	public void sendMessage(ActorHandle from, ActorHandle to, Object message) {
		sender.sendMessage(from, to, message);
	}

}
