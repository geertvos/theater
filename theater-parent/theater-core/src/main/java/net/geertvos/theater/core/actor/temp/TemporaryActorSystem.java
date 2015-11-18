package net.geertvos.theater.core.actor.temp;

import static net.geertvos.theater.core.networking.SegmentMessageTypes.ACTOR_MESSAGE;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import net.geertvos.gossip.api.cluster.Cluster;
import net.geertvos.gossip.api.cluster.ClusterEventListener;
import net.geertvos.gossip.api.cluster.ClusterMember;
import net.geertvos.theater.api.actors.Actor;
import net.geertvos.theater.api.actors.ActorHandle;
import net.geertvos.theater.api.management.ActorSystem;
import net.geertvos.theater.api.serialization.Serializer;
import net.geertvos.theater.core.networking.SegmentClient;
import net.geertvos.theater.core.networking.SegmentClientFactory;
import net.geertvos.theater.core.networking.SegmentMessage;
import net.geertvos.theater.core.util.UUIDGen;
import net.geertvos.theater.kryo.serialization.KryoSerializer;

public class TemporaryActorSystem implements ActorSystem, ClusterEventListener {

	private static final Logger log = Logger.getLogger(TemporaryActorSystem.class);
	private final Map<String,Actor> actors = new HashMap<String,Actor>();
	private List<ClusterMember> clusterMembers = null;
	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	private final Lock readLock = lock.readLock();
	private final Lock writeLock = lock.writeLock();
	
	private final String clusterId;
	private final Serializer serializer = new KryoSerializer();
	
	private final SegmentClientFactory clientFactory;
	private final Map<String,SegmentClient> clients = new ConcurrentHashMap<String, SegmentClient>();
	
	public TemporaryActorSystem(Cluster cluster, SegmentClientFactory clientFactory) {
		this.clientFactory = clientFactory;
		this.clusterId = cluster.getLocalMember().getId();
		cluster.getEventService().registerListener(this);
	}
	
	public void registerActor(Actor actor) {
		actors.put(actor.getType(), actor);
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
		Actor actor = actors.get(to.getType());
		Object state = actor.onCreate(to);
		actor.onActivate(to, state);
		actor.onMessage(from, to, message, state);
		actor.onDeactivate(to, state);
		actor.onDestroy(to, state);
	}

	private void sendMessageToOtherMember(ActorHandle from, ActorHandle to, Object message) {
		boolean sent = false;
		TempActorHandle tempID = (TempActorHandle) to;
		for(ClusterMember member : clusterMembers) {
			if(member.getId().equals(tempID.getMemberId())) {
				//send
				SegmentClient client = clients.get(member.getId());
				if(client != null) {
					UUID messageId = UUIDGen.makeType1UUIDFromHost(UUIDGen.getLocalAddress());
					SegmentMessage internalMessage = new SegmentMessage(ACTOR_MESSAGE.ordinal(), messageId, from, to);
					if(message != null) {
						byte[] unEncodedData = serializer.serialize(message);
						if(unEncodedData != null) {
							String data = Base64.encodeBase64String(unEncodedData);
							internalMessage.setParameter("payload", data);
						} else {
							log.error("Serialization of message failed.");
							return;
						}
					} else {
						log.warn("Sending message without payload.");
					}
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
	}
	
	public void onNewInactiveMember(ClusterMember member,List<ClusterMember> members) {}
	
	public void onMemberActivated(ClusterMember member,	List<ClusterMember> members) {
		SegmentClient client = clientFactory.createClient(member);
		client.start();
		clients.put(member.getId(), client);
	}

	
	public void onClusterDestabilized(List<ClusterMember> members) {}

	public void onMemberDeactivated(ClusterMember member, List<ClusterMember> members) {
		SegmentClient client = clients.remove(member.getId());
		if(client != null) {
			client.stop();
		}
	}

	
}
