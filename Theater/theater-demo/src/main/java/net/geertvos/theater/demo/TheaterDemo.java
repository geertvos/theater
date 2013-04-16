package net.geertvos.theater.demo;

import net.geertvos.gossip.api.cluster.ClusterMember;
import net.geertvos.gossip.core.GossipCluster;
import net.geertvos.gossip.core.GossipClusterMember;
import net.geertvos.gossip.core.network.GossipServer;
import net.geertvos.theater.api.actors.Actor;
import net.geertvos.theater.api.actorstore.ActorStore;
import net.geertvos.theater.api.factory.ActorFactory;
import net.geertvos.theater.api.messaging.Message;
import net.geertvos.theater.core.partitioning.ClusteredPartitionManager;
import net.geertvos.theater.core.partitioning.LocalPartition;
import net.geertvos.theater.core.partitioning.LocalPartitionFactory;
import net.geertvos.theater.core.partitioning.RemotePartition;
import net.geertvos.theater.core.partitioning.RemotePartitionFactory;

import org.apache.log4j.BasicConfigurator;

public class TheaterDemo {

	private static final String CLUSTER = "demoCluster";
	
	public static void main(String[] args) {
		BasicConfigurator.configure();
		int number = 1;
		int knows = 1;
		if(args.length == 2 ) {
			number = Integer.parseInt(args[0]);
			knows = Integer.parseInt(args[1]);
		}
		
		GossipClusterMember member = new GossipClusterMember("Member-"+knows, "localhost", 8000+knows, System.currentTimeMillis(),"");
		GossipCluster cluster = new GossipCluster(CLUSTER, "Member-"+number, "localhost", 8000+number, member );
		GossipServer server = new GossipServer(cluster);
		server.start();

		final ActorFactory factory = new ActorFactory() {
			
			public Actor createActor(Message input) {
				return null;
			}
		};

		final ActorStore store = null;

		LocalPartitionFactory local = new LocalPartitionFactory() {

			public LocalPartition createPartition(int id) {
				return new LocalPartition(id, factory, store);
			}
		};
		RemotePartitionFactory remote = new RemotePartitionFactory() {

			public RemotePartition createPartition(int id, ClusterMember host) {
				return new RemotePartition(id, host);
			}
		};
		ClusteredPartitionManager partitionManager = new ClusteredPartitionManager(8, cluster, local, remote);

	}
	
}
