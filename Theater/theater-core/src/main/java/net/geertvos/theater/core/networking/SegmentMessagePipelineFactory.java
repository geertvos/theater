package net.geertvos.theater.core.networking;

import net.geertvos.theater.api.management.Theater;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.frame.LengthFieldBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.LengthFieldPrepender;

public class SegmentMessagePipelineFactory implements ChannelPipelineFactory {

	private final Theater actorCluster;

	public SegmentMessagePipelineFactory(Theater manager) {
		this.actorCluster = manager;
	}

	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = Channels.pipeline();
		pipeline.addLast("messageEncoder", new SegmentMessageEncoder());
		pipeline.addLast("lengthEncoder", new LengthFieldPrepender(4));
		pipeline.addLast("lengthDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,0,4,0,4));
		pipeline.addLast("messageDecoder", new SegmentMessageDecoder());
		pipeline.addLast("handler", new SegmentMessageHandler(actorCluster));
		return pipeline;
	}

}
