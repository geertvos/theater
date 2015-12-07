package net.geertvos.theater.api.clustering;

public interface GroupMembershipProvider {

	public void registerListener(GroupMembershipListener listener);

	public GroupMember getLocalMember();
	
}
