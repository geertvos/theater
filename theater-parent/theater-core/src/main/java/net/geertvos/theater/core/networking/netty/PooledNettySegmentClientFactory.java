package net.geertvos.theater.core.networking.netty;

import java.util.concurrent.ConcurrentHashMap;

import net.geertvos.gossip.api.cluster.ClusterMember;
import net.geertvos.theater.core.networking.SegmentClient;
import net.geertvos.theater.core.networking.SegmentClientFactory;

public class PooledNettySegmentClientFactory implements SegmentClientFactory {

	private final ConcurrentHashMap<String, NettySegmentClient> clients = new ConcurrentHashMap<String, NettySegmentClient>();

	public SegmentClient createClient(int segment, ClusterMember member) {
		int port = Integer.parseInt(member.getMetaData("segmentServer.port"));
		return getClient(member.getHost(), port);
	}
	
	private NettySegmentClient getClient(String host, int port) {
		String key = host+"/"+port;
		NettySegmentClient client = clients.get(key);
		if(client != null) {
			return clients.get(key);
		}
		NettySegmentClient newClient = new NettySegmentClient(host, port);
		NettySegmentClient existing = clients.putIfAbsent(key, new NettySegmentClient(host, port));
		if(existing==null) {
			return newClient;
		} else {
			return existing;
		}
	}

	public SegmentClient createClient(ClusterMember member) {
		int port = Integer.parseInt(member.getMetaData("segmentServer.port"));
		return getClient(member.getHost(), port);
	}

}
