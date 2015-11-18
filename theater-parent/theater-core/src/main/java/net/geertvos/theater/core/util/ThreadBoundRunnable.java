package net.geertvos.theater.core.util;

public interface ThreadBoundRunnable<T> extends Runnable {

		public T getKey();
		
}
