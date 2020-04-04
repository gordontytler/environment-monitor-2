package monitor.implementation.session;

import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import monitor.model.Configuration;

public class SessionManager implements Runnable {

	static Logger logger = Logger.getLogger(SessionManager.class.getName());
	
	private boolean logFine = Configuration.getInstance().isLogFine();
	//private boolean logFine = true;
	
	private AllSessionPools allSessionPools = AllSessionPools.getInstance();
	private ReentrantLock sessionManagementLock;
	
	private SessionManagementRunnable[] runnables;
	private Thread[] threads;
	private long delayBetweenRuns = 20000;	// this does not need to be in config
	private boolean keepRunning = true;
	
	/** Starts threads to manage sessions and checks that they are not stuck. */
	public SessionManager() {
		perpareThreads();
	}
	
	private void perpareThreads() {
		sessionManagementLock = new ReentrantLock(true);		
		runnables = new SessionManagementRunnable[] {
				new SessionReaper(sessionManagementLock),
				new FinishedActionSessionCloser(sessionManagementLock, 10000),
				new ClosedSessionTester(sessionManagementLock, 20000)			
		};
		for (SessionManagementRunnable runnable : runnables) {
			runnable.setMaxExpectedDelayBetweenRuns(runnable.getDelayBetweenRuns() + 180000);
		}
		threads = new Thread[] {
				new Thread(runnables[0], "SessionReaper"),
				new Thread(runnables[1], "FinishedActionSessionCloser"),
				new Thread(runnables[2], "ClosedSessionTester")
		};
	}

	void interruptThreads() {
		for (int i=0; i < threads.length; i++) {
			logger.severe(String.format("about to interrupt thread %s after %d runs.", threads[i].getName(), runnables[i].runCount));
			threads[i].interrupt();
			runnables[i].stop();
		}
	}
	
	private void startThreads() {
		for (Thread thread : threads) {
			thread.start();
		}
	}	
	
	@Override
	public void run() {
		logger.info("SessionManager is running.");
		startThreads();
		if (logFine) logger.info("threads started. ShutdownThreadIsRunning is " + allSessionPools.isShutdownThreadIsRunning());		
		sleepBetweenRuns();
		long loopCount = 0;
		while (!allSessionPools.isShutdownThreadIsRunning()) {
			try {
				for (int i=0; i < threads.length; i++) {
					if ((System.currentTimeMillis() -  runnables[i].getTimeLastRan()) > runnables[i].getMaxExpectedDelayBetweenRuns()) {
						logger.severe(String.format("thread %s appears to be stuck. It last ran %d millis ago.", threads[i].getName(),
								System.currentTimeMillis() - runnables[i].getTimeLastRan()));
						logAllThreadsInfo(i);
						interruptThreads();
						perpareThreads();
						startThreads();
						break;
					}
				}
				if (logFine || ++loopCount % 30 == 0) logAllThreadsInfo(-1);
				sleepBetweenRuns();				
				
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
			}
		}
	}

	
	private void sleepBetweenRuns() {
		try {
			if (logFine) logger.info("\n\nSessionManager is sleeping for " + delayBetweenRuns);
			Thread.sleep(delayBetweenRuns);
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
	}
	
	private void logAllThreadsInfo(int indexToIgnore) {
		for (int j=0; j < threads.length; j++) {
			if (j != indexToIgnore) {
				logger.info(String.format("Run %d of thread %s was %d millis ago. Will be considered stuck after %d", runnables[j].runCount, threads[j].getName(),
						System.currentTimeMillis() - runnables[j].getTimeLastRan(), runnables[j].getMaxExpectedDelayBetweenRuns()));
			}
		}
	}

	// for tests - makes the threads stop so that restarting stuck threads can be tested
	
	public ReentrantLock getSessionManagementLock() {
		return sessionManagementLock;
	}

	public void setSessionManagementLock(ReentrantLock sessionManagementLock) {
		this.sessionManagementLock = sessionManagementLock;
	}

	public SessionManagementRunnable[] getRunnables() {
		return runnables;
	}

	public void setRunnables(SessionManagementRunnable[] runnables) {
		this.runnables = runnables;
	}

	public Thread[] getThreads() {
		return threads;
	}

	public void setThreads(Thread[] threads) {
		this.threads = threads;
	}

	public long getDelayBetweenRuns() {
		return delayBetweenRuns;
	}

	public void setDelayBetweenRuns(long delayBetweenRuns) {
		this.delayBetweenRuns = delayBetweenRuns;
	}

	public void setLogFine(boolean logFine) {
		this.logFine = logFine;
	}

	public boolean isKeepRunning() {
		return keepRunning;
	}

	public void setKeepRunning(boolean keepRunning) {
		this.keepRunning = keepRunning;
	}
	
	
}
