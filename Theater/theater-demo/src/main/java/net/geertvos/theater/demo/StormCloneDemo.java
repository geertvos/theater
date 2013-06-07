package net.geertvos.theater.demo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import me.prettyprint.cassandra.service.ThriftKsDef;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.ddl.ColumnFamilyDefinition;
import me.prettyprint.hector.api.ddl.ComparatorType;
import me.prettyprint.hector.api.ddl.KeyspaceDefinition;
import me.prettyprint.hector.api.factory.HFactory;
import net.geertvos.gossip.core.GossipCluster;
import net.geertvos.gossip.core.GossipClusterMember;
import net.geertvos.gossip.core.network.GossipServer;
import net.geertvos.theater.api.actors.ActorId;
import net.geertvos.theater.api.actorstore.ActorStateStore;
import net.geertvos.theater.api.durability.MessageLog;
import net.geertvos.theater.api.durability.SegmentMessageLogFactory;
import net.geertvos.theater.api.management.ActorSystem;
import net.geertvos.theater.api.management.Theater;
import net.geertvos.theater.cassandra.actorstore.CassandraActorDao;
import net.geertvos.theater.cassandra.actorstore.CassandraActorStore;
import net.geertvos.theater.cassandra.durability.CassandraMessageLog;
import net.geertvos.theater.cassandra.durability.CassandraMessageLogDao;
import net.geertvos.theater.core.actor.ActorIdImpl;
import net.geertvos.theater.core.actor.temp.TemporaryActorSystem;
import net.geertvos.theater.core.management.TheaterImpl;
import net.geertvos.theater.core.messaging.SegmentMessageSender;
import net.geertvos.theater.core.networking.PooledSegmentClient;
import net.geertvos.theater.core.networking.TheaterServer;
import net.geertvos.theater.core.segmentation.SegmentActorSystem;

import org.apache.log4j.BasicConfigurator;

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
		
		//Cassandra setup
		Cluster myCluster = HFactory.getOrCreateCluster("Geert Cluster", "192.168.5.104:9160");
		ColumnFamilyDefinition cfDef = HFactory.createColumnFamilyDefinition(THEATER_KEYSPACE, MESSAGE_LOG_COLUMNFAMILIY, ComparatorType.BYTESTYPE);
		ColumnFamilyDefinition actorStoreDef = HFactory.createColumnFamilyDefinition(THEATER_KEYSPACE, ACTOR_STORE_COLUMNFAMILIY, ComparatorType.BYTESTYPE);
		KeyspaceDefinition newKeyspace = HFactory.createKeyspaceDefinition(THEATER_KEYSPACE, ThriftKsDef.DEF_STRATEGY_CLASS, 1, Arrays.asList(cfDef,actorStoreDef));
		KeyspaceDefinition keyspaceDef = myCluster.describeKeyspace(THEATER_KEYSPACE);
		if(keyspaceDef == null) {
			myCluster.addKeyspace(newKeyspace, true);
		}
		Keyspace ksp = HFactory.createKeyspace(THEATER_KEYSPACE, myCluster);
		
		//Gossip cluster setup
		Map<String,String> meta = new HashMap<String,String>();
		meta.put("segmentServer.port", "500"+number);

		final GossipClusterMember member = new GossipClusterMember("Member-"+knows, "localhost", 8000+knows, System.currentTimeMillis(),"");
		final GossipCluster cluster = new GossipCluster(CLUSTER, "Member-"+number, "localhost", 8000+number, meta, member );
		final GossipServer server = new GossipServer(cluster);
		server.start();

		//Actor Framework setup
		final CassandraMessageLogDao messageLogDao = new CassandraMessageLogDao(ksp, MESSAGE_LOG_COLUMNFAMILIY);
		final CassandraActorDao actorDao = new CassandraActorDao(ksp, ACTOR_STORE_COLUMNFAMILIY);
		final ActorStateStore store = new CassandraActorStore(actorDao);

		SegmentMessageLogFactory logFactory = new SegmentMessageLogFactory() {
			
			public MessageLog createLog(int segment) {
				return new CassandraMessageLog(segment, messageLogDao);
			}
		};
		final Theater theater = new TheaterImpl();
		final SegmentMessageSender sender = new SegmentMessageSender(theater);

		final PooledSegmentClient clients = new PooledSegmentClient();
		final SegmentActorSystem segmentedActorSystem = new SegmentActorSystem(logFactory,store,512,cluster,clients);
		segmentedActorSystem.registerActor(new LineSpout(sender), "linesprout");
		segmentedActorSystem.registerActor(new WordCountBolt(sender), "wordcountbolt");

		ActorSystem temporarySystem = new TemporaryActorSystem(cluster,clients);
		temporarySystem.registerActor(new EchoActor(), "echo");

		theater.registerActorSystem("segmented", segmentedActorSystem);
		theater.registerActorSystem("temp", temporarySystem);
		
		final TheaterServer segmentServer = new TheaterServer("localhost", 5000+number, theater);
		segmentServer.start();
		
		//Kick off the system by sending the first message
		if(number == 1) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			final ActorId sproutId = new ActorIdImpl(CLUSTER, "segmented", "linesprout",UUID.randomUUID());
			sender.sendMessage(null, sproutId, new CreateSprout());
		}
	}
	
}
