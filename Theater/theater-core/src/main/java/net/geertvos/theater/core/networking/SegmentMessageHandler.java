package net.geertvos.theater.core.networking;

import java.io.IOException;

import net.geertvos.theater.api.partitioning.Segment;
import net.geertvos.theater.api.partitioning.SegmentManager;
import net.geertvos.theater.core.segmenting.LocalSegment;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

public class SegmentMessageHandler extends SimpleChannelHandler {

	private Logger log = Logger.getLogger(SegmentMessageHandler.class);
	private final SegmentManager manager;
	
	public SegmentMessageHandler(SegmentManager manager) {
		this.manager = manager;
	}
	
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		SegmentMessage message = (SegmentMessage) e.getMessage();
		Segment partition = manager.findSegmentForActor(message.getTo());
		if(partition instanceof LocalSegment) {
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
