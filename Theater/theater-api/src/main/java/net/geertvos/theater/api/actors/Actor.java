package net.geertvos.theater.api.actors;

import net.geertvos.theater.api.messaging.Message;

public interface Actor {

	void onCreate();
	
	void onActivate();
	
	void onDeactivate();
	
	void onDestroy();
	
	ActorId getId();
	
	void handleMessage(Message message);
	
}
