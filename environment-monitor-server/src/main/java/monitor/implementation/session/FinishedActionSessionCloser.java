package monitor.implementation.session;


import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import monitor.model.CommandStatus;
import monitor.model.Configuration;
import monitor.model.SessionType;

/** 
 * An action session usually does one long running command like tailing a log file.
 * If the last command status is finished this indicates it has crashed so we can close the session.
 * Other actions, like discover applications, will do several commands that will each finish before the next one runs
 * so the unusedMillis has to weigh the risk of ending when it has not finished against creating too many sessions.
 * 
 * This would not be needed if the python script always called close when done.
 */
public class FinishedActionSessionCloser extends SessionManagementRunnable implements Runnable {

	static Logger logger = Logger.getLogger(SessionReaper.class.getName());
	private static final boolean logFine = Configuration.getInstance().isLogFine();
	
	long unusedMillis = Configuration.getInstance().getUnusedMillisBeforeCloseOfFinishedActionSession();
	long delayBetweenRuns = 20000; 

	private AllSessionPools allSessionPools = AllSessionPools.getInstance();
	private ReentrantLock sessionManagementLock;
	private long delayBeforeRunning;
	
	public FinishedActionSessionCloser(ReentrantLock sessionManagementLock, int delayBeforeRunning) {
		this.sessionManagementLock = sessionManagementLock;
		this.delayBeforeRunning = delayBeforeRunning;		
	}

	public void run() {
		boolean interrupted = false;
		super.setLastActivityTime(System.currentTimeMillis() + delayBeforeRunning);
		try {
			Thread.sleep(delayBeforeRunning);
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE, e.toString(), e);
			interrupted = true;
		}

		boolean changeOnLastLoop = true;
		while (!allSessionPools.isShutdownThreadIsRunning() && super.keepRunning && !interrupted) {
			if (changeOnLastLoop) {
				logger.info("Checking for unused finished action sessions every " + delayBetweenRuns + "ms. This is run " + super.getRunCount());
			}
			changeOnLastLoop = false;
			long closeTime = System.currentTimeMillis() - unusedMillis;
			try {
				sessionManagementLock.lockInterruptibly();
				for (Map.Entry<String, Session> entry : allSessionPools.getAllSessions().entrySet()) {
					Session session = entry.getValue();
					if (session.getLastUsed() < closeTime && 
						!session.isControlSession() &&
						session.getSessionType() == SessionType.ACTION &&
						(session.getLastCommandStatus() == CommandStatus.FINISHED || session.getLastCommandStatus() == null ) &&
						session.isOpen()) {
						if (logFine) logger.info("\n\nClosing " + session);
						session.close("FinishedActionSessionCloser");
						changeOnLastLoop = true;
					}
					super.setLastActivityTime(System.currentTimeMillis());
				}
				sessionManagementLock.unlock();
				Thread.sleep(delayBetweenRuns);
			} catch (InterruptedException e) {
				logger.log(Level.SEVERE, e.toString(), e);
				interrupted = true;
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Problem ending unused finished action sessions", e);
			} finally {
				if (sessionManagementLock.isHeldByCurrentThread()) {
					sessionManagementLock.unlock();
				}
			}				
			super.setLastActivityTime(System.currentTimeMillis());
		}
		logger.severe("\n\nFinishedActionSessionCloser has finished. keepRunning is " + keepRunning + " ShutdownThreadIsRunning is " + allSessionPools.isShutdownThreadIsRunning() + " interrupted is " + interrupted);		
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
