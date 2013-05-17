package net.geertvos.theater.demo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import me.prettyprint.cassandra.service.ThriftKsDef;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.ddl.ColumnFamilyDefinition;
import me.prettyprint.hector.api.ddl.ComparatorType;
import me.prettyprint.hector.api.ddl.KeyspaceDefinition;
import me.prettyprint.hector.api.factory.HFactory;
import net.geertvos.gossip.api.cluster.ClusterMember;
import net.geertvos.gossip.core.GossipCluster;
import net.geertvos.gossip.core.GossipClusterMember;
import net.geertvos.gossip.core.network.GossipServer;
import net.geertvos.theater.api.actors.Actor;
import net.geertvos.theater.api.actors.ActorId;
import net.geertvos.theater.api.actorstore.ActorStore;
import net.geertvos.theater.api.durability.MessageLog;
import net.geertvos.theater.api.factory.ActorFactory;
import net.geertvos.theater.api.messaging.Message;
import net.geertvos.theater.cassandra.actorstore.CassandraActorDao;
import net.geertvos.theater.cassandra.actorstore.CassandraActorStore;
import net.geertvos.theater.cassandra.durability.CassandraMessageLog;
import net.geertvos.theater.cassandra.durability.CassandraMessageLogDao;
import net.geertvos.theater.core.actor.ActorIdImpl;
import net.geertvos.theater.core.messaging.SegmentMessageSender;
import net.geertvos.theater.core.networking.SegmentMessage;
import net.geertvos.theater.core.networking.SegmentServer;
import net.geertvos.theater.core.segmentation.ClusteredSegmentManager;
import net.geertvos.theater.core.segmentation.LocalSegment;
import net.geertvos.theater.core.segmentation.LocalSegmentFactory;
import net.geertvos.theater.core.segmentation.RemoteSegment;
import net.geertvos.theater.core.segmentation.RemoteSegmentFactory;
import net.geertvos.theater.core.util.ThreadBoundExecutorService;
import net.geertvos.theater.core.util.ThreadBoundRunnable;
import net.geertvos.theater.core.util.UUIDGen;

import org.apache.log4j.BasicConfigurator;

public class RemoteDemo {

	private static final String MESSAGE_LOG_COLUMNFAMILIY = "messageLog";
	private static final String THEATER_KEYSPACE = "theater";
	private static final String CLUSTER = "demoCluster";
	private static final String ACTOR_STORE_COLUMNFAMILIY = "actorStore";
	
	public static void main(String[] args) {
		BasicConfigurator.configure();
		int number = 3;
		int knows = 2;
		
		Cluster myCluster = HFactory.getOrCreateCluster("Geert Cluster", "192.168.5.104:9160");
		ColumnFamilyDefinition cfDef = HFactory.createColumnFamilyDefinition(THEATER_KEYSPACE, MESSAGE_LOG_COLUMNFAMILIY, ComparatorType.BYTESTYPE);
		ColumnFamilyDefinition actorStoreDef = HFactory.createColumnFamilyDefinition(THEATER_KEYSPACE, ACTOR_STORE_COLUMNFAMILIY, ComparatorType.BYTESTYPE);
		KeyspaceDefinition newKeyspace = HFactory.createKeyspaceDefinition(THEATER_KEYSPACE, ThriftKsDef.DEF_STRATEGY_CLASS, 1, Arrays.asList(cfDef,actorStoreDef));
		KeyspaceDefinition keyspaceDef = myCluster.describeKeyspace(THEATER_KEYSPACE);
		if(keyspaceDef == null) {
			myCluster.addKeyspace(newKeyspace, true);
		}

		Keyspace ksp = HFactory.createKeyspace(THEATER_KEYSPACE, myCluster);
		
		final CassandraMessageLogDao<SegmentMessage> messageLogDao = new CassandraMessageLogDao<SegmentMessage>(ksp, MESSAGE_LOG_COLUMNFAMILIY, SegmentMessage.class);
		
		GossipClusterMember member = new GossipClusterMember("Member-"+knows, "192.168.5.104", 8000+knows, System.currentTimeMillis(),"");
		Map<String,String> meta = new HashMap<String,String>();
		meta.put("segmentServer.port", "500"+number);
		GossipCluster cluster = new GossipCluster(CLUSTER, "Member-"+number, "192.168.5.227", 8000+number, meta, member );
		GossipServer server = new GossipServer(cluster);
		server.start();

		final ActorFactory factory = new ActorFactory() {
			
			public Actor createActor(final Message input) {
					return new TheaterActor(input.getTo());
			}
		};
		CassandraActorDao actorDao = new CassandraActorDao(ksp, ACTOR_STORE_COLUMNFAMILIY, TheaterActor.class);
		final ActorStore store = new CassandraActorStore(actorDao);
		final ThreadBoundExecutorService<ThreadBoundRunnable<UUID>, UUID> executor = new ThreadBoundExecutorService<ThreadBoundRunnable<UUID>, UUID>(10);

		LocalSegmentFactory local = new LocalSegmentFactory() {

			public LocalSegment createSegment(int id) {
				MessageLog log = new CassandraMessageLog(id, messageLogDao);
				return new LocalSegment(id, factory, store, log, executor);
			}
		};
		RemoteSegmentFactory remote = new RemoteSegmentFactory() {

			public RemoteSegment createSegment(int id, ClusterMember host) {
				MessageLog log = new CassandraMessageLog(id, messageLogDao);
				return new RemoteSegment(id, host, log);
			}
		};
		ClusteredSegmentManager segmentManager = new ClusteredSegmentManager(8, cluster, local, remote);
		SegmentServer segmentServer = new SegmentServer("192.168.5.227", 5000+number, segmentManager);
		segmentServer.start();
		
		final SegmentMessageSender sender = new SegmentMessageSender(segmentManager);
		final ActorId to = new ActorIdImpl(UUID.randomUUID(), CLUSTER);
		final ActorId from = new ActorIdImpl(UUID.randomUUID(), CLUSTER);

			final AtomicInteger integer = new AtomicInteger();
			Timer t = new Timer();
			t.scheduleAtFixedRate(new TimerTask() {
				
				@Override
				public void run() {
					UUID messageId = UUIDGen.makeType1UUIDFromHost(UUIDGen.getLocalAddress());
					final SegmentMessage message = new SegmentMessage(1, messageId, from, to);
					message.setParameter("counter", String.valueOf(integer.incrementAndGet()));
					sender.sendMessage(message);
				}
			}, 600, 1000);

	}
	
}
