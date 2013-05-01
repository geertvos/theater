package net.geertvos.theater.core.networking;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.frame.LengthFieldBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.LengthFieldPrepender;

public class PartitionClientPipelineFactory implements ChannelPipelineFactory {

	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = Channels.pipeline();
		pipeline.addLast("lengthEncoder", new LengthFieldPrepender(4));
		pipeline.addLast("messageEncoder", new PartitionMessageEncoder());
		pipeline.addLast("lengthDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,0,4,0,4));
		pipeline.addLast("messageDecoder", new PartitionMessageDecoder());
		return pipeline;
	}

}
