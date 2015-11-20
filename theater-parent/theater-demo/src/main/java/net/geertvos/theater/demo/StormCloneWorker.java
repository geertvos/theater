package net.geertvos.theater.demo;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.BasicConfigurator;

import net.geertvos.gossip.core.GossipCluster;
import net.geertvos.gossip.core.GossipClusterMember;
import net.geertvos.gossip.core.network.GossipServer;
import net.geertvos.theater.core.management.TheaterBuilder;

public class StormCloneWorker {

	private static final String CLUSTER = "stormClone";
	
	public static void main(String[] args) {
		BasicConfigurator.configure();
		
		//Gossip cluster setup
		Map<String,String> meta = new HashMap<String,String>();
		meta.put("segmentServer.port", "5001");

		final GossipClusterMember member = new GossipClusterMember("Master", "localhost", 8000, System.currentTimeMillis(),"");
		final GossipCluster cluster = new GossipCluster(CLUSTER, "Worker", "localhost", 8001, meta, member );
		final GossipServer server = new GossipServer(cluster);
		server.start();

		TheaterBuilder builder = new TheaterBuilder();
		builder.divideInSegments(4)
			   .useHost("localhost")
			   .onPort(5001)
			   .withClustering(cluster)
			   .build();
	}
	
}
