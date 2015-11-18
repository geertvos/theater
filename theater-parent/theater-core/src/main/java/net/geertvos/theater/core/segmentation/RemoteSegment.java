package net.geertvos.theater.core.segmentation;

import static net.geertvos.theater.core.networking.SegmentMessageTypes.ACTOR_MESSAGE;

import java.util.UUID;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import net.geertvos.gossip.api.cluster.ClusterMember;
import net.geertvos.theater.api.actors.ActorHandle;
import net.geertvos.theater.api.segmentation.Segment;
import net.geertvos.theater.core.networking.SegmentClient;
import net.geertvos.theater.core.networking.SegmentMessage;
import net.geertvos.theater.core.util.UUIDGen;
import net.geertvos.theater.kryo.serialization.KryoSerializer;

public class RemoteSegment implements Segment {

	private Logger log = Logger.getLogger(RemoteSegment.class);
	private final KryoSerializer serializer = new KryoSerializer();
	private final ClusterMember clusterMember;
	private final int id;
	private final SegmentClient client;
	private volatile boolean operational = false;
	
	public RemoteSegment(int id, ClusterMember clusterMember, SegmentClient client) {
		this.id = id;
		this.clusterMember = clusterMember;
		this.client = client;
	}


	public int getId() {
		return id;
	}

	public void onInit() {
		operational = true;
		client.start();
	}

	public void onDestroy() {
		operational = false;
	}
	
	public ClusterMember getClusterMember() {
		return clusterMember;
	}

	public boolean isLocal() {
		return false;
	}

	public boolean isOperational() {
		return operational;
	}

	public void handleMessage(ActorHandle from, ActorHandle to, Object message) {
		log.info("RemoteSegment is handling message from "+from+" to "+to);
		UUID messageId = UUIDGen.makeType1UUIDFromHost(UUIDGen.getLocalAddress());
		SegmentMessage internalMessage = new SegmentMessage(ACTOR_MESSAGE.ordinal(), messageId, from, to);
		if(message != null) {
			byte[] unEncodedData = serializer.serialize(message);
			if(unEncodedData != null) {
				String data = Base64.encodeBase64String(unEncodedData);
				internalMessage.setParameter("payload", data);
				this.client.sendMessage(internalMessage);
			} else {
				log.error("Serialization of message failed.");
				return;
			}
		} else {
			log.warn("Sending message without payload.");
		}
	}

}
