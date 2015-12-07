package net.geertvos.theater.demo;

import net.geertvos.theater.api.clustering.gossip.GossipGroupMembershipProvider;
import net.geertvos.theater.api.clustering.gossip.GossipGroupMembershipProviderBuilder;
import net.geertvos.theater.core.management.TheaterBuilder;

import org.apache.log4j.BasicConfigurator;

public class StormCloneWorker {

	private static final int port = 5001;
	
	public static void main(String[] args) {
		BasicConfigurator.configure();

		//Create the Group Membership Provider for the Worker node. The worker has the master as seed node. 
		GossipGroupMembershipProvider provider = new GossipGroupMembershipProviderBuilder("Storm Clone", "Worker")
		.onPort(8001)
		.withMetadata("segmentServer.port", String.valueOf(port))
		.withSeedMember("Master", 8000)
		.build();

		//Create the Theater Actor Framework and start the show.
		new TheaterBuilder()
		       .divideInSegments(4)
			   .useHost("localhost")
			   .onPort(port)
			   .withGroupMemberShipProvider(provider)
			   .build();
	}
	
}
