package net.geertvos.theater.api.segmentation;

import net.geertvos.theater.api.messaging.Message;

public interface Segment {

	boolean isLocal();
	
	boolean isOperational();
	
	void onInit();
	
	void onDestroy();
	
	void handleMessage(Message message);
	
	int getId();
}
