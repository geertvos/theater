package net.geertvos.theater.api.partitioning;

import net.geertvos.theater.api.messaging.Message;

public interface Partition {

	boolean isLocal();
	
	boolean isOperational();
	
	void onInit();
	
	void onDestroy();
	
	void handleMessage(Message message);
	
	int getId();
}
