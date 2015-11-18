package net.geertvos.theater.demo;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.BasicConfigurator;

import net.geertvos.gossip.core.GossipCluster;
import net.geertvos.gossip.core.GossipClusterMember;
import net.geertvos.gossip.core.network.GossipServer;
import net.geertvos.theater.api.actors.ActorHandle;
import net.geertvos.theater.api.actorstore.ActorStateStore;
import net.geertvos.theater.api.management.ActorSystem;
import net.geertvos.theater.api.management.Theater;
import net.geertvos.theater.core.actor.ActorHandleImpl;
import net.geertvos.theater.core.actor.temp.TemporaryActorSystem;
import net.geertvos.theater.core.management.TheaterImpl;
import net.geertvos.theater.core.networking.SegmentClientFactory;
import net.geertvos.theater.core.networking.TheaterServer;
import net.geertvos.theater.core.networking.netty.PooledNettySegmentClientFactory;
import net.geertvos.theater.core.segmentation.SegmentedActorSystem;

public class StormCloneDemo {

	private static final String MESSAGE_LOG_COLUMNFAMILIY = "messageLog";
	private static final String THEATER_KEYSPACE = "stormClone";
	private static final String CLUSTER = "stormClone";
	private static final String ACTOR_STORE_COLUMNFAMILIY = "actorStore";
	
	public static void main(String[] args) {
		BasicConfigurator.configure();
		
		int number = 1;
		int knows = 2;
		if(args.length == 2 ) {
			number = Integer.parseInt(args[0]);
			knows = Integer.parseInt(args[1]);
		}
		
//		//Cassandra setup
//		Cluster myCluster = HFactory.getOrCreateCluster("Geert Cluster", "192.168.5.104:9160");
//		ColumnFamilyDefinition cfDef = HFactory.createColumnFamilyDefinition(THEATER_KEYSPACE, MESSAGE_LOG_COLUMNFAMILIY, ComparatorType.BYTESTYPE);
//		ColumnFamilyDefinition actorStoreDef = HFactory.createColumnFamilyDefinition(THEATER_KEYSPACE, ACTOR_STORE_COLUMNFAMILIY, ComparatorType.BYTESTYPE);
//		KeyspaceDefinition newKeyspace = HFactory.createKeyspaceDefinition(THEATER_KEYSPACE, ThriftKsDef.DEF_STRATEGY_CLASS, 1, Arrays.asList(cfDef,actorStoreDef));
//		KeyspaceDefinition keyspaceDef = myCluster.describeKeyspace(THEATER_KEYSPACE);
//		if(keyspaceDef == null) {
//			myCluster.addKeyspace(newKeyspace, true);
//		}
//		Keyspace ksp = HFactory.createKeyspace(THEATER_KEYSPACE, myCluster);
		
		//Gossip cluster setup
		Map<String,String> meta = new HashMap<String,String>();
		meta.put("segmentServer.port", "5000");

		final GossipClusterMember member = new GossipClusterMember("Worker", "localhost", 8001, System.currentTimeMillis(),"");
		final GossipCluster cluster = new GossipCluster(CLUSTER, "Master", "localhost", 8000, meta, member );
		final GossipServer server = new GossipServer(cluster);
		server.start();

		//Actor Framework setup
//		final CassandraMessageLogDao messageLogDao = new CassandraMessageLogDao(ksp, MESSAGE_LOG_COLUMNFAMILIY);
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

		final Theater theater = new TheaterImpl();

		SegmentClientFactory clientFactory = new PooledNettySegmentClientFactory();
		final SegmentedActorSystem segmentedActorSystem = new SegmentedActorSystem(store, 4, cluster, clientFactory);
		segmentedActorSystem.registerActor(new LineSpout(theater));
		segmentedActorSystem.registerActor(new WordCountBolt(theater));

		ActorSystem temporarySystem = new TemporaryActorSystem(cluster,clientFactory);
		temporarySystem.registerActor(new EchoActor());

		theater.registerActorSystem("segmented", segmentedActorSystem);
		theater.registerActorSystem("temp", temporarySystem);
		
		final TheaterServer segmentServer = new TheaterServer("localhost", 5000, theater);
		segmentServer.start();
		
		//Kick off the system by sending the first message
		if(number == 1) {
			try {
				Thread.sleep(6000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Sending the message.");
			final ActorHandle sproutId = new ActorHandleImpl(CLUSTER, "segmented", "linespout", UUID.randomUUID());
			theater.sendMessage(null, sproutId, new CreateSprout());
		}
//		segmentServer.shutdown();
	}
	
}
