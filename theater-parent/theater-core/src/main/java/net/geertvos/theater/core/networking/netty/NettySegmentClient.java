package net.geertvos.theater.core.networking.netty;

import static net.geertvos.theater.core.networking.SegmentMessageTypes.ACTOR_MESSAGE;

import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import net.geertvos.theater.api.actors.ActorHandle;
import net.geertvos.theater.api.messaging.Message;
import net.geertvos.theater.api.serialization.Serializer;
import net.geertvos.theater.core.networking.SegmentClient;
import net.geertvos.theater.core.networking.SegmentMessage;
import net.geertvos.theater.core.util.UUIDGen;
import net.geertvos.theater.kryo.serialization.KryoSerializer;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

public class NettySegmentClient implements SegmentClient {

	private static final Logger LOG = Logger.getLogger(NettySegmentClient.class);
	private final ClientBootstrap clientBootstrap;
	private final String host;
	private final int port;
	private final AtomicBoolean running = new AtomicBoolean();
	private final AtomicBoolean connected = new AtomicBoolean();
	private final BlockingQueue<Message> messageQueue = new LinkedBlockingQueue<Message>();
	private final Serializer serializer = new KryoSerializer();
	
	private ChannelFuture channelFuture;
	
	public NettySegmentClient(String host, int port) {
		this.port = port;
		this.host = host;
		this.clientBootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(Executors.newFixedThreadPool(1), Executors.newFixedThreadPool(1)));
		this.clientBootstrap.setPipelineFactory(new SegmentClientPipelineFactory());
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
	
	private void sendMessage(final Message message) {
		if(running.get()) {
			LOG.debug("Trying to send remote message: "+message.toString());
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
									LOG.debug("Writing message: "+message.toString());
									future.getChannel().write(message).addListener(new ChannelFutureListener() {
										
										public void operationComplete(ChannelFuture future) throws Exception {
											if(future.isSuccess()) {
												LOG.debug("Transmitted message: "+message.toString());
											} else {
												LOG.debug("Failed to transmit message.");
												connected.set(false);
											}
										}
									});
								} else {
									LOG.error("Unable to send message.", future.getCause());
									connected.set(false);
								}
							}
						});
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

				} else {
					LOG.info("Connecting client.");
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

	public void sendMessage(ActorHandle from, ActorHandle to, Object message) {
		UUID messageId = UUIDGen.makeType1UUIDFromHost(UUIDGen.getLocalAddress());
		SegmentMessage internalMessage = new SegmentMessage(ACTOR_MESSAGE.ordinal(), messageId, from, to);
		byte[] unEncodedData = serializer.serialize(message);
		if(unEncodedData != null) {
			String data = Base64.encodeBase64String(unEncodedData);
			internalMessage.setParameter("payload", data);
			sendMessage(internalMessage);
		} else {
			LOG.error("Serialization of message failed.");
			return;
		}
	}
	
}
