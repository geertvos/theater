package net.geertvos.theater.demo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.apache.log4j.Logger;

import net.geertvos.theater.api.actors.ActorHandle;
import net.geertvos.theater.api.management.Theater;
import net.geertvos.theater.api.messaging.MessageSender;
import net.geertvos.theater.core.actor.AbstractActorAdapter;
import net.geertvos.theater.core.actor.ActorHandleImpl;
import net.geertvos.theater.core.actor.temp.TempActorHandle;

public class LineSpout extends AbstractActorAdapter {

	protected LineSpout(Theater theater) {
		super(theater);
	}

	private Logger LOG = Logger.getLogger(LineSpout.class);
	private static final int MSGS_COUNT = 10;
	private static final int WORKER_COUNT = 100;

	private long start;
	private Random random = new Random(System.currentTimeMillis());
	
	public Object onCreate(ActorHandle actor) {
		start = System.currentTimeMillis();
		Firehose hose = new Firehose(actor);
		Thread t = new Thread(hose,"Firehose thread");
		t.start();
		return new LineSpoutState();
	}

	
	public void onActivate(ActorHandle actor, Object actorState) {
	}

	public void onDeactivate(ActorHandle actor, Object actorState) {
	}

	public void onMessage(ActorHandle actor, ActorHandle from, Object message, Object actorState) {
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

		private ActorHandle actor;
		
		public Firehose(ActorHandle actor) {
			this.actor = actor;
		}
		
		public void run() {
			List<UUID> processors = new ArrayList<UUID>(WORKER_COUNT);
			for(int i=0;i<WORKER_COUNT;i++) {
				processors.add(UUID.randomUUID());
			}

			for(int i=0;i<MSGS_COUNT;i++) {
				UUID processor = processors.get(random.nextInt(WORKER_COUNT));
				ActorHandle processorId = new ActorHandleImpl(actor.getCluster(), actor.getSystem(), "wordcountbolt", processor);
				Line line = new Line(UUID.randomUUID().toString(),"The quick brown fox jumped over the lazy fence");
				LOG.debug("Sending a new line to "+processor);
				getTheater().sendMessage(actor, processorId, line);
			}
			ActorHandle tempId = new TempActorHandle(actor.getCluster(),"temp","echo",UUID.randomUUID(),"Member-1");
			getTheater().sendMessage(actor, tempId, "hoi");
			ActorHandle tempId2 = new TempActorHandle(actor.getCluster(),"temp","echo",UUID.randomUUID(),"Member-2");
			getTheater().sendMessage(actor, tempId2, "hoi 2");
		}
		
	}

	public String getType() {
		return "linespout";
	}

}
