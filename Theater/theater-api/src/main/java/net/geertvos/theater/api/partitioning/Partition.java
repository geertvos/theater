package net.geertvos.theater.api.partitioning;

import net.geertvos.theater.api.messaging.Message;

public interface Partition {

	void handleMessage(Message message);
	
	int getId();
}
