package net.geertvos.theater.core.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ThreadBoundExecutorService<T extends ThreadBoundRunnable<U>, U> {

	private final int threads;
	private final List<BlockingQueue<T>> queues;
	
	public ThreadBoundExecutorService(int threads) {
		this.threads = threads;
		queues = new ArrayList<BlockingQueue<T>>(threads);
		for(int i=0;i<threads;i++) {
			BlockingQueue<T> q = new LinkedBlockingQueue<T>();
			queues.add(q);
		}
		for(int i=0; i<threads; i++) {
			ThreadBoundWorker w = new ThreadBoundWorker(queues.get(i));
			Thread thread = new Thread(w, "ThreadBounderWorker-"+i);
			thread.start();
		}
	}
	
	public void submit(T task) {
		int bucket = Math.abs(hash(task.getKey())) % threads;
		queues.get(bucket).add(task);
	}

	private int hash(U key) {
		return key.hashCode();
	}
	
	private class ThreadBoundWorker implements Runnable {

		private final BlockingQueue<T> queue;

		public ThreadBoundWorker(BlockingQueue<T> queue) {
			this.queue = queue;
		}
		
		public void run() {
			while(true) {
				try {
					T t = queue.take();
					t.run();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
}
