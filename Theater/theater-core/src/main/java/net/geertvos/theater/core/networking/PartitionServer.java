package net.geertvos.theater.core.networking;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import net.geertvos.theater.api.partitioning.PartitionManager;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

/**
 * @author Geert Vos
 */
public class PartitionServer {

	private final ServerBootstrap serverBootstrap;
	private Channel serverChannel;
	private final String host;
	private final int port;
	
	//TODO: Make server port configurable through meta data
	
	public PartitionServer(String host, int port, PartitionManager manager) {
		this.host = host;
		this.port = port;
		serverBootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
		serverBootstrap.setPipelineFactory(new PartitionMessagePipelineFactory(manager));
	}

	public void start() {
		serverChannel = serverBootstrap.bind(new InetSocketAddress(host, port));
	}
	
	public void shutdown() {
		serverChannel.close();
	}
	
}
