package net.geertvos.theater.core.networking;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import net.geertvos.theater.api.management.Theater;
import net.geertvos.theater.core.networking.netty.SegmentMessagePipelineFactory;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

/**
 * @author Geert Vos
 */
public class TheaterServer {

	private final ServerBootstrap serverBootstrap;
	private Channel serverChannel;
	private final String host;
	private final int port;
	
	public TheaterServer(String host, int port, Theater cluster) {
		this.host = host;
		this.port = port;
		serverBootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
		serverBootstrap.setPipelineFactory(new SegmentMessagePipelineFactory(cluster));
	}

	public void start() {
		serverChannel = serverBootstrap.bind(new InetSocketAddress(host, port));
	}
	
	public void shutdown() {
		serverChannel.close();
		serverBootstrap.releaseExternalResources();
	}
	
}
