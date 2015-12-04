package net.geertvos.theater.demo;

import java.util.HashMap;
import java.util.Map;

import net.geertvos.gossip.core.GossipCluster;
import net.geertvos.gossip.core.GossipClusterBuilder;
import net.geertvos.theater.core.management.TheaterBuilder;

import org.apache.log4j.BasicConfigurator;

public class StormCloneWorker {

	public static void main(String[] args) {
		BasicConfigurator.configure();
		
		Map<String,String> meta = new HashMap<String,String>();
		meta.put("segmentServer.port", "5001");

		GossipClusterBuilder clusterbuilder = new GossipClusterBuilder();
		clusterbuilder.clusterName("Storm Clone") 
					  .withMetadata(meta)
					  .memberName("Worker")
					  .onPort(8001)
					  .withSeedMember("Master", 8000);
		GossipCluster cluster = clusterbuilder.build();

		
		TheaterBuilder builder = new TheaterBuilder();
		builder.divideInSegments(4)
			   .useHost("localhost")
			   .onPort(5001)
			   .withClustering(cluster)
			   .build();
	}
	
}
