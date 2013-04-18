package net.geertvos.theater.core.networking;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import net.geertvos.theater.api.messaging.Message;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.testng.log4testng.Logger;

public class PartitionClient {

	private Logger log = Logger.getLogger(PartitionClient.class);
	
	private ClientBootstrap clientBootstrap;
	private ChannelFuture channelFuture;
	
	//TODO add reconnect logic
	
	public PartitionClient(String host, int port) {
		clientBootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
		clientBootstrap.setPipelineFactory(new PartitionClientPipelineFactory());
		channelFuture = clientBootstrap.connect(new InetSocketAddress(host, port));
	}
	
	public void sendMessage(final Message message) {
		channelFuture.addListener(new ChannelFutureListener() {
			
			public void operationComplete(ChannelFuture arg0) throws Exception {
				if(arg0.isSuccess()) {
					arg0.getChannel().write(message);
				} else {
					log.error("Unable to send message.", arg0.getCause());
				}
			}
		});
	}

	public void disconnect() {
		channelFuture.getChannel().close();
	}
	
}
