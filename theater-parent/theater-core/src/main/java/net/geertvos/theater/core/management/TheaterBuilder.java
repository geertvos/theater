package net.geertvos.theater.core.management;

import java.util.concurrent.ConcurrentHashMap;

import net.geertvos.gossip.api.cluster.Cluster;
import net.geertvos.theater.api.actors.ActorHandle;
import net.geertvos.theater.api.actorstore.ActorStateStore;
import net.geertvos.theater.api.management.ActorSystem;
import net.geertvos.theater.api.management.Theater;
import net.geertvos.theater.core.actor.temp.TemporaryActorSystem;
import net.geertvos.theater.core.networking.SegmentClientFactory;
import net.geertvos.theater.core.networking.TheaterServer;
import net.geertvos.theater.core.networking.netty.PooledNettySegmentClientFactory;
import net.geertvos.theater.core.segmentation.SegmentedActorSystem;

public class TheaterBuilder {

	private Cluster cluster;
	private int segments = 4;
	private int theaterServerPort = 5000;
	private String theaterServerHost = "localhost";
	
	public TheaterBuilder useHost(String host) {
		this.theaterServerHost = host;
		return this;
	}
	
	public TheaterBuilder onPort(int port) {
		this.theaterServerPort = port;
		return this;
	}
	
	public TheaterBuilder divideInSegments(int segments) {
		this.segments = segments;
		return this;
	}
	
	public TheaterBuilder withClustering(Cluster cluster) {
		this.cluster = cluster;
		return this;
	}
	
	public Theater build() {
		TheaterImpl theater = new TheaterImpl(cluster);

		//Actor Framework setup
//		final CassandraActorDao actorDao = new CassandraActorDao(ksp, ACTOR_STORE_COLUMNFAMILIY);
//		final ActorStateStore store = new CassandraActorStore(actorDao);
		final ActorStateStore store = new ActorStateStore() {
			
			private final ConcurrentHashMap<ActorHandle, Object> states = new ConcurrentHashMap<ActorHandle, Object>();
			
			public void writeActorState(ActorHandle actorHandle, Object actorState) {
				states.put(actorHandle, actorState);
			}
			
			public Object readActorState(ActorHandle actorHandle) {
				return states.get(actorHandle);
			}
		};
		
		SegmentClientFactory clientFactory = new PooledNettySegmentClientFactory();
		SegmentedActorSystem segmentedActorSystem = new SegmentedActorSystem(theater, store, segments, cluster, clientFactory);
		ActorSystem temporarySystem = new TemporaryActorSystem(theater, cluster,clientFactory);
		theater.registerActorSystem("segmented", segmentedActorSystem);
		theater.registerActorSystem("temp", temporarySystem);
		
		final TheaterServer segmentServer = new TheaterServer(theaterServerHost, theaterServerPort, theater);
		segmentServer.start();

		return theater;
	}
	
}
