package monitor.implementation.session;

import java.util.concurrent.atomic.AtomicLong;

public class NextSessionId implements SessionIdMaker {

	private static AtomicLong sessionCounter = new AtomicLong(0);

	public String makeNewSessionId() {
		return String.valueOf(sessionCounter.getAndIncrement());
	}		
	
}
