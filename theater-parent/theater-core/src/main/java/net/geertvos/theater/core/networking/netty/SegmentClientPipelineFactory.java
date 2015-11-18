package net.geertvos.theater.core.networking.netty;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.frame.LengthFieldBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.LengthFieldPrepender;

public class SegmentClientPipelineFactory implements ChannelPipelineFactory {

	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = Channels.pipeline();
		pipeline.addLast("lengthEncoder", new LengthFieldPrepender(4));
		pipeline.addLast("messageEncoder", new SegmentMessageEncoder());
		pipeline.addLast("lengthDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,0,4,0,4));
		pipeline.addLast("messageDecoder", new SegmentMessageDecoder());
		return pipeline;
	}

}
