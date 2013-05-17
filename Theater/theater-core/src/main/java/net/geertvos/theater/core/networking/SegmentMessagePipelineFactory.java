package net.geertvos.theater.core.networking;

import net.geertvos.theater.api.segmentation.SegmentManager;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.frame.LengthFieldBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.LengthFieldPrepender;

public class SegmentMessagePipelineFactory implements ChannelPipelineFactory {

	private final SegmentManager manager;

	public SegmentMessagePipelineFactory(SegmentManager manager) {
		this.manager = manager;
	}

	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = Channels.pipeline();
		pipeline.addLast("messageEncoder", new SegmentMessageEncoder());
		pipeline.addLast("lengthEncoder", new LengthFieldPrepender(4));
		pipeline.addLast("lengthDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,0,4,0,4));
		pipeline.addLast("messageDecoder", new SegmentMessageDecoder());
		pipeline.addLast("handler", new SegmentMessageHandler(manager));
		return pipeline;
	}

}
