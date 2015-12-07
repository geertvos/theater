package net.geertvos.theater.api.clustering.gossip;

import java.util.Map;

import net.geertvos.gossip.core.GossipCluster;
import net.geertvos.gossip.core.GossipClusterBuilder;

public class GossipGroupMembershipProviderBuilder {

	private GossipClusterBuilder clusterBuilder;
	
	public GossipGroupMembershipProviderBuilder(String groupName, String nodename) {
		clusterBuilder = new GossipClusterBuilder();
		clusterBuilder.clusterName(groupName);
		clusterBuilder.memberName(nodename);
	}
	
	
	public GossipGroupMembershipProviderBuilder withMetadata(Map<String,String> meta) {
		clusterBuilder.withMetadata(meta);
		return this;
	}
	
	public GossipGroupMembershipProviderBuilder withSeedMember(String name, String host, int port) {
		clusterBuilder.withSeedMember(name, host, port);
		return this;
	}
	
	public GossipGroupMembershipProviderBuilder withSeedMember(String name, int port) {
		clusterBuilder.withSeedMember(name, port);
		return this;
	}
	
	public GossipGroupMembershipProvider build() {
		GossipCluster cluster = clusterBuilder.build();
		return new GossipGroupMembershipProvider(cluster);
	}


	public GossipGroupMembershipProviderBuilder onPort(int i) {
		clusterBuilder.onPort(i);
		return this;
	}
	
	public GossipGroupMembershipProviderBuilder onHost(String host) {
		clusterBuilder.onHost(host);
		return this;
	}
	
}
