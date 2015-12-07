package net.geertvos.theater.demo;

import java.util.UUID;

import net.geertvos.theater.api.actors.ActorHandle;
import net.geertvos.theater.api.clustering.gossip.GossipGroupMembershipProvider;
import net.geertvos.theater.api.clustering.gossip.GossipGroupMembershipProviderBuilder;
import net.geertvos.theater.api.management.Theater;
import net.geertvos.theater.core.management.TheaterBuilder;
import net.geertvos.theater.demo.messages.CreateSpoutMessage;
import net.geertvos.theater.demo.spouts.LineSpoutActor;

import org.apache.log4j.BasicConfigurator;

public class StormCloneDemo {

	private static int port = 5000;

	public static void main(String[] args) {
		BasicConfigurator.configure();
		
		GossipGroupMembershipProvider provider = new GossipGroupMembershipProviderBuilder("Storm Clone", "Master")
			.withMetadata("segmentServer.port", String.valueOf(port))
			.withSeedMember("Worker", 8001)
			.build();
		
		final Theater theater =  new TheaterBuilder()
			   .divideInSegments(4)
			   .onPort(port)
			   .withGroupMemberShipProvider(provider)
			   .build();
		
		//Kick off the system by sending the first message
		try {
			Thread.sleep(6000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		ActorHandle sproutId = theater.getActor(LineSpoutActor.class, UUID.randomUUID());
		theater.sendMessage(null, sproutId, new CreateSpoutMessage());
	}
	
}
