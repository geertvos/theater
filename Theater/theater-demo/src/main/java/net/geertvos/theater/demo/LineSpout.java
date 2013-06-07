package net.geertvos.theater.demo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.apache.log4j.Logger;

import net.geertvos.theater.api.actors.ActorId;
import net.geertvos.theater.api.messaging.MessageSender;
import net.geertvos.theater.core.actor.AbstractActorAdapter;
import net.geertvos.theater.core.actor.ActorIdImpl;
import net.geertvos.theater.core.actor.temp.TempActorId;

public class LineSpout extends AbstractActorAdapter {

	private Logger LOG = Logger.getLogger(LineSpout.class);
	private static final int MSGS_COUNT = 1000;
	private static final int WORKER_COUNT = 100;

	private MessageSender sender;
	private long start;
	private Random random = new Random(System.currentTimeMillis());
	
	public LineSpout(MessageSender sender) {
		this.sender = sender;
	}
	
	public Object onCreate(ActorId actor) {
		start = System.currentTimeMillis();
		Firehose hose = new Firehose(actor);
		Thread t = new Thread(hose,"Firehose thread");
		t.start();
		return new LineSpoutState();
	}

	
	public void onActivate(ActorId actor, Object actorState) {
	}

	public void onDeactivate(ActorId actor, Object actorState) {
	}

	public void handleMessage(ActorId actor, ActorId from, Object message, Object actorState) {
		LineSpoutState state = (LineSpoutState)actorState;
		if(message instanceof LineResult) {
			state.setLineCount(state.getLineCount()+1);
			if(state.getLineCount()==MSGS_COUNT) {
				long time = System.currentTimeMillis() - start;
				System.out.println(actor.getId()+" Processed "+state.getLineCount()+" lines in "+time+"ms");
			} else {
				System.out.println(actor.getId()+" Processed "+state.getLineCount()+" lines so far.");
			}
		}
	}

	private class Firehose implements Runnable {

		private ActorId actor;
		
		public Firehose(ActorId actor) {
			this.actor = actor;
		}
		
		public void run() {
			List<UUID> processors = new ArrayList<UUID>(WORKER_COUNT);
			for(int i=0;i<WORKER_COUNT;i++) {
				processors.add(UUID.randomUUID());
			}

			for(int i=0;i<MSGS_COUNT;i++) {
				UUID processor = processors.get(random.nextInt(WORKER_COUNT));
				ActorId processorId = new ActorIdImpl(actor.getCluster(), actor.getSystem(), "wordcountbolt", processor);
				Line line = new Line(UUID.randomUUID().toString(),"The quick brown fox jumped over the lazy fence");
				LOG.debug("Sending a new line to "+processor);
				sender.sendMessage(actor, processorId, line);
			}
			ActorId tempId = new TempActorId(actor.getCluster(),"temp","echo",UUID.randomUUID(),"Member-1");
			sender.sendMessage(actor, tempId, "hoi");
			ActorId tempId2 = new TempActorId(actor.getCluster(),"temp","echo",UUID.randomUUID(),"Member-2");
			sender.sendMessage(actor, tempId2, "hoi 2");
		}
		
	}

}
