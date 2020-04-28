package monitor.implementation.session;


import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import monitor.model.Configuration;

/** 
 * Checks that closed sessions in the session pool are still working to avoid delay
 * when the session is opened by ServerSessionPool.findClosedSessionAndOpenIt
 */
public class ClosedSessionTester  extends SessionManagementRunnable implements Runnable {

	static Logger logger = Logger.getLogger(SessionReaper.class.getName());
	private static final boolean logFine = Configuration.getInstance().isLogFine();
	
	long closedSessionTestFrequencyMillis = Configuration.getInstance().getClosedSessionTestFrequencyMillis();
	long delayBetweenRuns = closedSessionTestFrequencyMillis;

	private AllSessionPools allSessionPools = AllSessionPools.getInstance();
	private ReentrantLock sessionManagementLock;
	private long delayBeforeRunning;
	
	public ClosedSessionTester(ReentrantLock sessionManagementLock, int delayBeforeRunning) {
		this.sessionManagementLock = sessionManagementLock;
		this.delayBeforeRunning = delayBeforeRunning;
	}

	@Override
	public void run() {
		boolean interrupted = false;
		super.setLastActivityTime(System.currentTimeMillis() + delayBeforeRunning);
		try {
			Thread.sleep(delayBeforeRunning);
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE, e.toString(), e);
			interrupted = true;
		}
		logger.info("Testing closed sessions every " + delayBetweenRuns + "ms.");
		while (!allSessionPools.isShutdownThreadIsRunning() && super.keepRunning && !interrupted) {
			long testTime = System.currentTimeMillis() - closedSessionTestFrequencyMillis;
			try {
				sessionManagementLock.lockInterruptibly();
				int testCount = 0;
				for (Map.Entry<String, Session> entry : allSessionPools.getAllSessions().entrySet()) {
					Session session = entry.getValue();
					if (!session.isOpen() && session.isLoggedOn() && 
							session.getLastUsed() < testTime && session.getLastTested() < testTime ) {
						if (logFine) logger.info("\n\nTesting " + session);
						String message = "was locked for opening so could not be tested";
						long startTime = System.currentTimeMillis();
						if (session.lockForOpening()) {
							message = "success";
							try {
								session.testTerminal();
								session.updateLastTested();
							} catch (Exception e) {
								message = e.getMessage();
								logger.log(Level.SEVERE, "Problem testing closed session:" + session.toStatusString(), e);
							}
							session.unlockForOpening();
							
							testCount++;
						}
						SessionEvent sessionEvent = new SessionEvent(System.currentTimeMillis(), "ClosedSessionTester", session.getSessionId(), session.isLoggedOn(), session.isOpen(), session.isControlSession(), session.getSessionType(), 
								message + " - took " + (System.currentTimeMillis() - startTime) + "ms", session.getLastUsed());
						session.appendToSessionHistory(sessionEvent);
					}
					super.setLastActivityTime(System.currentTimeMillis());
				}
				sessionManagementLock.unlock();
				if (testCount > 0) logger.info("Tested " + testCount + " closed sessions.");				
				Thread.sleep(delayBetweenRuns);
			} catch (InterruptedException e) {
				logger.log(Level.SEVERE, e.toString(), e);
				interrupted = true;
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Problem testing closed sessions", e);
			} finally {
				if (sessionManagementLock.isHeldByCurrentThread()) {
					sessionManagementLock.unlock();
				}
			}
			super.setLastActivityTime(System.currentTimeMillis());
		}
		logger.severe("\n\nClosedSessionTester has finished. keepRunning is " + keepRunning + " ShutdownThreadIsRunning is " + allSessionPools.isShutdownThreadIsRunning() + " interrupted is " + interrupted);		
	}

	@Override
	void setDelayBetweenRuns(long delayBetweenRuns) {
		this.delayBetweenRuns = delayBetweenRuns;
	}

	@Override
	void setDelayBeforeRunning(long delayBeforeRunning) {
		this.delayBeforeRunning = delayBeforeRunning;
	}	

	@Override
	long getDelayBetweenRuns() {
		return delayBetweenRuns;
	}	
}
