package net.geertvos.theater.core.actor.temp;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.geertvos.theater.api.actors.Actor;
import net.geertvos.theater.api.actors.ActorHandle;
import net.geertvos.theater.api.clustering.GroupMember;
import net.geertvos.theater.api.clustering.GroupMembershipListener;
import net.geertvos.theater.api.clustering.GroupMembershipProvider;
import net.geertvos.theater.api.management.ActorSystem;
import net.geertvos.theater.api.management.Theater;
import net.geertvos.theater.core.networking.SegmentClient;
import net.geertvos.theater.core.networking.SegmentClientFactory;

import org.apache.log4j.Logger;

public class TemporaryActorSystem implements ActorSystem, GroupMembershipListener {

	private static final Logger LOG = Logger.getLogger(TemporaryActorSystem.class);
	private List<GroupMember> clusterMembers = null;
	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	private final Lock readLock = lock.readLock();
	private final Lock writeLock = lock.writeLock();
	private final String clusterId;
	private final SegmentClientFactory clientFactory;
	private final Map<String,SegmentClient> clients = new ConcurrentHashMap<String, SegmentClient>();
	private final Theater theater;
	
	public TemporaryActorSystem(Theater theater, GroupMembershipProvider cluster, SegmentClientFactory clientFactory) {
		this.theater = theater;
		this.clientFactory = clientFactory;
		this.clusterId = cluster.getLocalMember().getId();
		cluster.registerListener(this);
	}
	
	public void handleMessage(ActorHandle from, ActorHandle to, Object message) {
		//TODO: Implement thread bound executor here too
		try {
			readLock.lock();
			if(to instanceof TempActorHandle){ 
				TempActorHandle tempID = (TempActorHandle) to;
				if(clusterId.equals(tempID.getMemberId())) {
					handleMessageInternally(from, to, message);
				} else {
					sendMessageToOtherMember(from, to, message);
				}
			}
		} finally {
			readLock.unlock();
		}
		
	}

	private void handleMessageInternally(ActorHandle from, ActorHandle to, Object message) {
		try {
			@SuppressWarnings("unchecked")
			Actor<Object> actor = (Actor<Object>) Class.forName(to.getType()).newInstance();
			actor.setTheater(theater);
			Object state = actor.onCreate(to);
			actor.onActivate(to, state);
			actor.onMessage(from, to, message, state);
			actor.onDeactivate(to, state);
			actor.onDestroy(to, state);
		} catch(Exception e) {
			LOG.error("Unable to handle message for temp actor.", e);
		}
	}

	private void sendMessageToOtherMember(ActorHandle from, ActorHandle to, Object message) {
		TempActorHandle tempID = (TempActorHandle) to;
		for(GroupMember member : clusterMembers) {
			if(member.getId().equals(tempID.getMemberId())) {
				SegmentClient client = clients.get(member.getId());
				client.sendMessage(from, to, message);
			}
		}
	}

	public void onClusterStabilized(List<GroupMember> members) {
		try {
			writeLock.lock();
			this.clusterMembers = members;
		} finally {
			writeLock.unlock();
		}
	}

	public void onNewActiveMember(GroupMember member,	List<GroupMember> members) {
	}
	
	public void onNewInactiveMember(GroupMember member,List<GroupMember> members) {}
	
	public void onMemberActivated(GroupMember member,	List<GroupMember> members) {
		SegmentClient client = clientFactory.createClient(member);
		client.start();
		clients.put(member.getId(), client);
	}

	
	public void onClusterDestabilized(List<GroupMember> members) {}

	public void onMemberDeactivated(GroupMember member, List<GroupMember> members) {
		SegmentClient client = clients.remove(member.getId());
		if(client != null) {
			client.stop();
		}
	}

	
}
