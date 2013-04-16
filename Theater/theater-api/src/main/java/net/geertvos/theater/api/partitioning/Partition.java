package net.geertvos.theater.api.partitioning;

import net.geertvos.theater.api.messaging.Message;

public interface Partition {

	void onInit();
	
	void onDestroy();
	
	void handleMessage(Message message);
	
	int getId();
}
