package net.geertvos.theater.core.networking;

import java.util.concurrent.ConcurrentHashMap;

public class PooledSegmentClient {

	
	private ConcurrentHashMap<String, SegmentClient> clients = new ConcurrentHashMap<String, SegmentClient>();
	
	public SegmentClient getClient(String host, int port) {
		String key = host+"/"+port;
		SegmentClient client = clients.get(key);
		if(client != null) {
			return clients.get(key);
		}
		SegmentClient newClient = new SegmentClient(host, port);
		SegmentClient existing = clients.putIfAbsent(key, new SegmentClient(host, port));
		if(existing==null) {
			return newClient;
		} else {
			return existing;
		}
	}
	
}
