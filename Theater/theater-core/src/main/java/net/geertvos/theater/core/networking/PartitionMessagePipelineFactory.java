package net.geertvos.theater.core.networking;

import net.geertvos.theater.api.partitioning.PartitionManager;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.Delimiters;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.codec.string.StringEncoder;

public class PartitionMessagePipelineFactory implements ChannelPipelineFactory {

	private final PartitionManager manager;

	public PartitionMessagePipelineFactory(PartitionManager manager) {
		this.manager = manager;
	}

	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = Channels.pipeline();
		pipeline.addLast("frameDecoder", new DelimiterBasedFrameDecoder(Integer.MAX_VALUE, Delimiters.lineDelimiter()));
		pipeline.addLast("decoder", new StringDecoder());
		pipeline.addLast("encoder", new StringEncoder());
		pipeline.addLast("messageDecoder", new PartitionMessageDecoder());
		pipeline.addLast("messageEncoder", new PartitionMessageEncoder());
		pipeline.addLast("handler", new PartitionMessageHandler(manager));
		return pipeline;
	}

}
