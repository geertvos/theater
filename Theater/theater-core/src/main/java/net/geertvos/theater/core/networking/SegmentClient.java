package net.geertvos.theater.core.networking;

import java.net.InetSocketAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import net.geertvos.theater.api.messaging.Message;

import org.apache.log4j.Logger;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

public class SegmentClient {

	private Logger log = Logger.getLogger(SegmentClient.class);
	
	private ClientBootstrap clientBootstrap;
	private ChannelFuture channelFuture;

	private String host;
	private int port;
	private AtomicBoolean running = new AtomicBoolean();
	private AtomicBoolean connected = new AtomicBoolean();
	private BlockingQueue<Message> messageQueue = new LinkedBlockingQueue<Message>();
	
	public SegmentClient(String host, int port) {
		this.port = port;
		this.host = host;
		clientBootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(Executors.newFixedThreadPool(1), Executors.newFixedThreadPool(1)));
		clientBootstrap.setPipelineFactory(new SegmentClientPipelineFactory());
	}
	
	public void start() {
		if(!running.get()) {
			running.set(true);
			connected.set(false);
			Thread thread = new Thread(new Worker(), "Segment client.");
			thread.start();
		}
	}

	public void stop() {
		if(running.get()) {
			running.set(false);
			messageQueue.clear();
			channelFuture.getChannel().close();
		}
	}
	
	public void sendMessage(final Message message) {
		if(running.get()) {
			log.debug("Trying to send remote message: "+message.toString());
			messageQueue.add(message);
		} else {
			throw new IllegalStateException("Trying to send a message, but the client is stopped.");
		}
	}

	public void disconnect() {
		channelFuture.getChannel().close().awaitUninterruptibly();
		clientBootstrap.releaseExternalResources();
	}
	
	private class Worker implements Runnable {

		public void run() {
			while(running.get()) {
				if(connected.get()) {
					try {
						final Message message = messageQueue.take();
						channelFuture.addListener(new ChannelFutureListener() {
							
							public void operationComplete(ChannelFuture future) throws Exception {
								if(future.isSuccess()) {
									log.debug("Writing message: "+message.toString());
									future.getChannel().write(message).addListener(new ChannelFutureListener() {
										
										public void operationComplete(ChannelFuture future) throws Exception {
											if(future.isSuccess()) {
												log.debug("Transmitted message: "+message.toString());
											} else {
												log.debug("Failed to transmit message.");
												connected.set(false);
											}
										}
									});
								} else {
									log.error("Unable to send message.", future.getCause());
									connected.set(false);
								}
							}
						});
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

				} else {
					log.info("Connecting client.");
					channelFuture = clientBootstrap.connect(new InetSocketAddress(host, port)).awaitUninterruptibly();
					channelFuture.addListener(new ChannelFutureListener() {
						
						public void operationComplete(ChannelFuture arg0) throws Exception {
							if(arg0.isSuccess()) {
								connected.set(true);
							}
						}
					});
				}
			}
		}
		
	}
	
}
