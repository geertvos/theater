package net.geertvos.theater.api.clustering;

import java.util.List;

/**
 * @author Geert Vos
 */
public interface GroupMembershipListener {

	/**
	 * This event will be called when a new member is discovered in the cluster
	 * that is currently online.
	 * 
	 * @param member The information about the member
	 */
	public void onNewActiveMember(GroupMember member, List<GroupMember> members);
	
	/**
	 * This event will be called when a new member is discovered in the cluster
	 * that is currently offline.
	 * 
	 * @param member The information about the member
	 */
	public void onNewInactiveMember(GroupMember member, List<GroupMember> members);
	
	/**
	 * An already known member changes state from offline to online
	 * 
	 * @param member The information about the member
	 */
	public void onMemberActivated(GroupMember member, List<GroupMember> members);
	
	/**
	 * An already known member changes state from online to offline
	 * 
	 * @param member The information about the member
	 */
	public void onMemberDeactivated(GroupMember member, List<GroupMember> members);
	
	/**
	 * This event will be called when all active members of the cluster see 
	 * the same list of active members in the cluster.
	 *  
	 * @param members The members in the cluster
	 */
	public void onClusterStabilized(List<GroupMember> members);
	
	/**
	 * This event will be called when the cluster is destabilized. This means
	 * that the cluster no longer has one shared view of the active members.
	 * This happens when new members are added or when existing members leave.
	 */
	public void onClusterDestabilized(List<GroupMember> members);
	
}
