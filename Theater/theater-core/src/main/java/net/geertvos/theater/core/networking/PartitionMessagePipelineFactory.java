package net.geertvos.theater.core.networking;

import net.geertvos.theater.api.partitioning.PartitionManager;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.frame.LengthFieldBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.LengthFieldPrepender;

public class PartitionMessagePipelineFactory implements ChannelPipelineFactory {

	private final PartitionManager manager;

	public PartitionMessagePipelineFactory(PartitionManager manager) {
		this.manager = manager;
	}

	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = Channels.pipeline();
		pipeline.addLast("messageEncoder", new PartitionMessageEncoder());
		pipeline.addLast("lengthEncoder", new LengthFieldPrepender(4));
		pipeline.addLast("lengthDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,0,4,0,4));
		pipeline.addLast("messageDecoder", new PartitionMessageDecoder());
		pipeline.addLast("handler", new PartitionMessageHandler(manager));
		return pipeline;
	}

}
