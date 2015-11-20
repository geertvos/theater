package net.geertvos.theater.demo;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.BasicConfigurator;

import net.geertvos.gossip.core.GossipCluster;
import net.geertvos.gossip.core.GossipClusterMember;
import net.geertvos.gossip.core.network.GossipServer;
import net.geertvos.theater.api.actors.ActorHandle;
import net.geertvos.theater.api.management.Theater;
import net.geertvos.theater.core.management.TheaterBuilder;

public class StormCloneDemo {

	private static final String MESSAGE_LOG_COLUMNFAMILIY = "messageLog";
	private static final String THEATER_KEYSPACE = "stormClone";
	private static final String CLUSTER = "stormClone";
	private static final String ACTOR_STORE_COLUMNFAMILIY = "actorStore";
	
	public static void main(String[] args) {
		BasicConfigurator.configure();
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

		TheaterBuilder builder = new TheaterBuilder();
		builder.divideInSegments(4)
			   .useHost("localhost")
			   .onPort(5000)
			   .withClustering(cluster);
		Theater theater = builder.build();

		//Kick off the system by sending the first message
		try {
			Thread.sleep(6000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		ActorHandle sproutId = theater.getActor(LineSpoutActor.class, UUID.randomUUID());
		theater.sendMessage(null, sproutId, new CreateSproutMessage());
	}
	
}
