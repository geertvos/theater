package net.geertvos.theater.demo;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.BasicConfigurator;

import net.geertvos.gossip.core.GossipCluster;
import net.geertvos.gossip.core.GossipClusterMember;
import net.geertvos.gossip.core.network.GossipServer;
import net.geertvos.theater.api.actors.ActorHandle;
import net.geertvos.theater.api.actorstore.ActorStateStore;
import net.geertvos.theater.api.management.ActorSystem;
import net.geertvos.theater.api.management.Theater;
import net.geertvos.theater.core.actor.temp.TemporaryActorSystem;
import net.geertvos.theater.core.management.TheaterImpl;
import net.geertvos.theater.core.networking.SegmentClientFactory;
import net.geertvos.theater.core.networking.TheaterServer;
import net.geertvos.theater.core.networking.netty.PooledNettySegmentClientFactory;
import net.geertvos.theater.core.segmentation.SegmentedActorSystem;

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

		//Actor Framework setup
		final ActorStateStore store = new ActorStateStore() {
			
			private final ConcurrentHashMap<ActorHandle, Object> states = new ConcurrentHashMap<ActorHandle, Object>();
			
			public void writeActorState(ActorHandle actorHandle, Object actorState) {
				states.put(actorHandle, actorState);
			}
			
			public Object readActorState(ActorHandle actorHandle) {
				return states.get(actorHandle);
			}
		};

		final Theater theater = new TheaterImpl();

		SegmentClientFactory clientFactory = new PooledNettySegmentClientFactory();
		final SegmentedActorSystem segmentedActorSystem = new SegmentedActorSystem(store, 4, cluster, clientFactory);
		segmentedActorSystem.registerActor(new LineSpout(theater));
		segmentedActorSystem.registerActor(new WordCountBolt(theater));

		ActorSystem temporarySystem = new TemporaryActorSystem(cluster, clientFactory);
		temporarySystem.registerActor(new EchoActor());

		theater.registerActorSystem("segmented", segmentedActorSystem);
		theater.registerActorSystem("temp", temporarySystem);
		
		final TheaterServer segmentServer = new TheaterServer("localhost", 5001, theater);
		segmentServer.start();
	}
	
}
