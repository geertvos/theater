package net.geertvos.theater.api.clustering.gossip;

import java.util.ArrayList;
import java.util.List;

import net.geertvos.gossip.api.cluster.ClusterEventListener;
import net.geertvos.gossip.api.cluster.ClusterMember;
import net.geertvos.gossip.core.GossipCluster;
import net.geertvos.theater.api.clustering.GroupMember;
import net.geertvos.theater.api.clustering.GroupMembershipListener;
import net.geertvos.theater.api.clustering.GroupMembershipProvider;

public class GossipGroupMembershipProvider implements GroupMembershipProvider  {

	private GossipCluster cluster;
	
	public GossipGroupMembershipProvider(GossipCluster cluster) {
		this.cluster = cluster;
	}
	
	public GroupMember getLocalMember() {
		return wrap(cluster.getLocalMember());
	}
	
	public void registerListener(GroupMembershipListener listener) {
		this.cluster.getEventService().registerListener(new ListenerWrapper(listener));
	}
	
	public class ListenerWrapper implements ClusterEventListener {
		
		private GroupMembershipListener originalListener;

		public ListenerWrapper(GroupMembershipListener listener) {
			this.originalListener = listener;
		}

		public void onNewActiveMember(ClusterMember member, List<ClusterMember> members) {
			originalListener.onNewActiveMember(wrap(member), wrap(members));
		}

		public void onNewInactiveMember(ClusterMember member, List<ClusterMember> members) {
			originalListener.onNewInactiveMember(wrap(member), wrap(members));
		}

		public void onMemberActivated(ClusterMember member, List<ClusterMember> members) {
			originalListener.onMemberActivated(wrap(member), wrap(members));
		}

		public void onMemberDeactivated(ClusterMember member, List<ClusterMember> members) {
			originalListener.onMemberDeactivated(wrap(member), wrap(members));
		}

		public void onClusterStabilized(List<ClusterMember> members) {
			originalListener.onClusterStabilized(wrap(members));
		}

		public void onClusterDestabilized(List<ClusterMember> members) {
			originalListener.onClusterDestabilized(wrap(members));
		}
		
	}
	
	public GroupMember wrap(ClusterMember member) {
		return new MemberWrapper(member);
	}
	
	public List<GroupMember> wrap(List<ClusterMember> members) {
		List<GroupMember> wrappedMembers = new ArrayList<GroupMember>(members.size());
		for(ClusterMember m : members) {
			wrappedMembers.add(wrap(m));
		}
		return wrappedMembers;
	}
	
	public class MemberWrapper implements GroupMember {

		private ClusterMember originalMember;
		
		public MemberWrapper(ClusterMember member) {
			this.originalMember = member;
		}
		
		public String getId() {
			return originalMember.getId();
		}

		public String getMetaData(String key) {
			return originalMember.getMetaData(key);
		}

		public String getHost() {
			return originalMember.getHost();
		}
	}

}
