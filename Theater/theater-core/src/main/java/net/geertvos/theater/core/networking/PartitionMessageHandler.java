package net.geertvos.theater.core.networking;

import java.io.IOException;

import net.geertvos.theater.api.partitioning.Partition;
import net.geertvos.theater.api.partitioning.PartitionManager;
import net.geertvos.theater.core.partitioning.LocalPartition;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

public class PartitionMessageHandler extends SimpleChannelHandler {

	private Logger log = Logger.getLogger(PartitionMessageHandler.class);
	private final PartitionManager manager;
	
	public PartitionMessageHandler(PartitionManager manager) {
		this.manager = manager;
	}
	
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		PartitionMessage message = (PartitionMessage) e.getMessage();
		Partition partition = manager.findPartitionForActor(message.getTo());
		if(partition instanceof LocalPartition) {
			partition.handleMessage(message);
		} else {
			//reply with error
			log.error("Receiving a message for a partition that is no longer on this host.");
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		if(e.getCause() instanceof IOException) {
			//ignore for now, we need to handle failed connections later.
		} else {
			e.getCause().printStackTrace();
		}
	}
	
}
