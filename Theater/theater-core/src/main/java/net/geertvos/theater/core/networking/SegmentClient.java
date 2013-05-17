package net.geertvos.theater.core.networking;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import net.geertvos.theater.api.messaging.Message;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.testng.log4testng.Logger;

public class SegmentClient {

	private Logger log = Logger.getLogger(SegmentClient.class);
	
	private ClientBootstrap clientBootstrap;
	private ChannelFuture channelFuture;

	private String host;
	private int port;
	
	public SegmentClient(String host, int port) {
		this.port = port;
		this.host = host;
		//TODO: Move to pooling
		clientBootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
		clientBootstrap.setPipelineFactory(new SegmentClientPipelineFactory());
		channelFuture = clientBootstrap.connect(new InetSocketAddress(host, port));
	}
	
	public void sendMessage(final Message message) {
		log.debug("Trying to send remote message: "+message.toString());
		if(!channelFuture.getChannel().isConnected()) {
			//todo: fix race condition
			channelFuture = clientBootstrap.connect(new InetSocketAddress(host, port));
		}
		channelFuture.addListener(new ChannelFutureListener() {
			
			public void operationComplete(ChannelFuture future) throws Exception {
				if(future.isSuccess()) {
					log.debug("Writing message: "+message.toString());
					future.getChannel().write(message);
				} else {
					log.error("Unable to send message.", future.getCause());
				}
			}
		});
	}

	public void disconnect() {
		channelFuture.getChannel().close();
	}
	
}
