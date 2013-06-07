package net.geertvos.theater.core.actor.temp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.geertvos.gossip.api.cluster.Cluster;
import net.geertvos.gossip.api.cluster.ClusterEventListener;
import net.geertvos.gossip.api.cluster.ClusterMember;
import net.geertvos.theater.api.actors.Actor;
import net.geertvos.theater.api.management.ActorSystem;
import net.geertvos.theater.api.messaging.Message;
import net.geertvos.theater.api.serialization.Deserializer;
import net.geertvos.theater.core.networking.PooledSegmentClient;
import net.geertvos.theater.core.networking.SegmentClient;
import net.geertvos.theater.kryo.serialization.KryoSerializer;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

public class TemporaryActorSystem implements ActorSystem, ClusterEventListener {

	private Logger log = Logger.getLogger(TemporaryActorSystem.class);
	private Map<String,Actor> actors = new HashMap<String,Actor>();
	private List<ClusterMember> clusterMembers = null;
	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	private final Lock readLock = lock.readLock();
	private final Lock writeLock = lock.writeLock();
	
	private String clusterId;
	private final Deserializer deserializer = new KryoSerializer();
	
	private final PooledSegmentClient clients;

	public TemporaryActorSystem(Cluster cluster, PooledSegmentClient clients) {
		this.clients = clients;
		this.clusterId = cluster.getLocalMember().getId();
		cluster.getEventService().registerListener(this);
	}
	
	public void registerActor(Actor actor, String id) {
		actors.put(id, actor);
	}

	public void handleMessage(Message message) {
		//TODO: Implement thread bound executor here too
		try {
			readLock.lock();
			if(message.getTo() instanceof TempActorId){ 
				TempActorId tempID = (TempActorId) message.getTo();
				if(clusterId.equals(tempID.getMemberId())) {
					handleMessageInternally(message, tempID);
				} else {
					sendMessageToOtherMember(tempID, message);
				}
			}
		} finally {
			readLock.unlock();
		}
		
	}

	private void handleMessageInternally(Message message, TempActorId tempID) {
		Actor actor = actors.get(tempID.getType());
		Object state = actor.onCreate(tempID);
		actor.onActivate(tempID, state);
		
		Object decodedMessage = null;
		String data = message.getParameter("payload");
		if(data != null) {
			byte[] bytes = Base64.decodeBase64(data);
			decodedMessage = deserializer.deserialize(bytes);
		} else {
			log.warn("Payload for temporary message is null.");
		}
		actor.handleMessage(tempID, message.getFrom(), decodedMessage, state);
		actor.onDeactivate(tempID, state);
		actor.onDestroy(tempID, state);
	}

	private void sendMessageToOtherMember(TempActorId tempID, Message message) {
		boolean sent = false;
		for(ClusterMember member : clusterMembers) {
			if(member.getId().equals(tempID.getMemberId())) {
				//send
				String host = member.getHost();
				int port = Integer.parseInt(member.getMetaData("segmentServer.port"));
				SegmentClient client = clients.getClient(host, port);
				if(client != null) {
					client.sendMessage(message);
					sent = true;
				} else {
					log.error("There is no client registered for member "+member.getId());
				}
			}
		}
		if(!sent) {
			log.warn("Cluster member "+tempID.getMemberId()+" is not or no longer part of this cluster.");
		}
	}

	public void onClusterStabilized(List<ClusterMember> members) {
		try {
			writeLock.lock();
			this.clusterMembers = members;
		} finally {
			writeLock.unlock();
		}
	}

	public void onNewActiveMember(ClusterMember member,	List<ClusterMember> members) {
		ensureClientExists(member);
	}
	
	public void onNewInactiveMember(ClusterMember member,List<ClusterMember> members) {}
	
	public void onMemberActivated(ClusterMember member,	List<ClusterMember> members) {
		ensureClientExists(member);
	}

	private void ensureClientExists(ClusterMember member) {
		String host = member.getHost();
		int port = Integer.parseInt(member.getMetaData("segmentServer.port"));
		clients.getClient(host, port);
	}
	
	public void onMemberDeactivated(ClusterMember member, List<ClusterMember> members) {
		String host = member.getHost();
		int port = Integer.parseInt(member.getMetaData("segmentServer.port"));
		String key = host+"/"+port;
	}
	
	public void onClusterDestabilized(List<ClusterMember> members) {}
	
}
