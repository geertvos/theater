package net.geertvos.theater.demo;

import java.util.HashMap;
import java.util.Map;

import net.geertvos.theater.api.clustering.gossip.GossipGroupMembershipProvider;
import net.geertvos.theater.api.clustering.gossip.GossipGroupMembershipProviderBuilder;
import net.geertvos.theater.core.management.TheaterBuilder;

import org.apache.log4j.BasicConfigurator;

public class StormCloneWorker {

	public static void main(String[] args) {
		BasicConfigurator.configure();
		
		Map<String,String> meta = new HashMap<String,String>();
		meta.put("segmentServer.port", "5001");

//		GossipClusterBuilder clusterbuilder = new GossipClusterBuilder();
//		clusterbuilder.clusterName("Storm Clone") 
//					  .withMetadata(meta)
//					  .memberName("Worker")
//					  .onPort(8001)
//					  .withSeedMember("Master", 8000);
//		GossipCluster cluster = clusterbuilder.build();
//		GossipGroupMembershipProvider provider = new GossipGroupMembershipProvider(cluster);
//		
		GossipGroupMembershipProvider provider = new GossipGroupMembershipProviderBuilder("Storm Clone", "Worker")
		.onPort(8001)
		.withMetadata(meta)
		.withSeedMember("Master", 8000)
		.build();

		
		TheaterBuilder builder = new TheaterBuilder();
		builder.divideInSegments(4)
			   .useHost("localhost")
			   .onPort(5001)
			   .withGroupMemberShipProvider(provider)
			   .build();
	}
	
}
