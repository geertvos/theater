package net.geertvos.theater.demo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.apache.log4j.Logger;

import net.geertvos.theater.api.actors.ActorHandle;
import net.geertvos.theater.core.actor.AbstractActorAdapter;

public class LineSpoutActor extends AbstractActorAdapter {

	private Logger LOG = Logger.getLogger(LineSpoutActor.class);
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
		if(message instanceof LineResultMessage) {
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
				ActorHandle processorId = getTheater().getActor(WordCountBoltActor.class, processor);
				LineMessage line = new LineMessage(UUID.randomUUID().toString(),"The quick brown fox jumped over the lazy fence");
				LOG.debug("Sending a new line to "+processor);
				getTheater().sendMessage(actor, processorId, line);
			}
			ActorHandle echo1 = getTheater().getTempActor(EchoActor.class, UUID.randomUUID());
			getTheater().sendMessage(actor, echo1, "hoi");
			ActorHandle echo2 = getTheater().getTempActor(EchoActor.class, UUID.randomUUID());
			getTheater().sendMessage(actor, echo2, "hoi 2");
		}
		
	}

}
