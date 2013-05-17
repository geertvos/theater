package net.geertvos.theater.core.networking;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import net.geertvos.theater.api.segmentation.SegmentManager;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

/**
 * @author Geert Vos
 */
public class SegmentServer {

	private final ServerBootstrap serverBootstrap;
	private Channel serverChannel;
	private final String host;
	private final int port;
	
	//TODO: Make server port configurable through meta data
	
	public SegmentServer(String host, int port, SegmentManager manager) {
		this.host = host;
		this.port = port;
		serverBootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
		serverBootstrap.setPipelineFactory(new SegmentMessagePipelineFactory(manager));
	}

	public void start() {
		serverChannel = serverBootstrap.bind(new InetSocketAddress(host, port));
	}
	
	public void shutdown() {
		serverChannel.close();
	}
	
}
