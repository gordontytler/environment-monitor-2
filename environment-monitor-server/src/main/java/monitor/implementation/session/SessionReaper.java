package monitor.implementation.session;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import monitor.model.Configuration;

public class SessionReaper extends SessionManagementRunnable implements Runnable {

	static Logger logger = Logger.getLogger(SessionReaper.class.getName());
	
	long closeMillis = Configuration.getInstance().getUnusedMinutesBeforeClosingSession() * 1000 * 60;
	long logoutMillis = Configuration.getInstance().getUnusedMinutesBeforeLogoutSession() * 1000 * 60;
	
	long delayBetweenRuns = closeMillis + 10000;

	private AllSessionPools allSessionPools = AllSessionPools.getInstance();

	private ReentrantLock sessionManagementLock;

	@SuppressWarnings("unused")
	private long delayBeforeRunning;
	
	public SessionReaper(ReentrantLock sessionManagementLock) {
		this.sessionManagementLock = sessionManagementLock;
	}

	public void run() {
		boolean interrupted = false;
		boolean changeOnLastLoop = true;
		while (!allSessionPools.isShutdownThreadIsRunning() && super.keepRunning && !interrupted) {
			if (changeOnLastLoop) {
				logger.info("Checking for unused sessions every " + delayBetweenRuns + "ms. This is run " + super.getRunCount());
			}
			changeOnLastLoop = false;
			long closeTime = System.currentTimeMillis() - closeMillis;
			long logoutTime = System.currentTimeMillis() - logoutMillis;
			try {
				sessionManagementLock.lockInterruptibly();
				for (Map.Entry<String, Session> entry : allSessionPools.getAllSessions().entrySet()) {
					Session session = entry.getValue();
					if (session.getLastUsed() < logoutTime && !session.isControlSession()) {
						logger.info("\n\nLogout " + session);
						if (session.isOpen()) {
							session.close("SessionReaper logout");
						}
						session.logout("SessionReaper logout");
						changeOnLastLoop = true;
					} else 	if (session.getLastUsed() < closeTime && session.isOpen()) {
						logger.info("\n\nClose " + session);
						session.close("SessionReaper close");
						changeOnLastLoop = true;
					}
				}

				// this is the fail safe check for connections left open due to a bug
				if (allSessionPools.getAllSessions().size() == 0) {
					for (Entry<String, ServerSessionPool> entry : allSessionPools.getAllServerSessionPools().entrySet()) {
						ServerSessionPool pool = entry.getValue();
						SSHConnection[] connections =pool.getSshConnections().toArray(new SSHConnection[pool.getSshConnections().size()]);
						for (SSHConnection connection : connections) {
							connection.destroy();
							pool.getSshConnections().remove(connection);
						}
					}
				}
				sessionManagementLock.unlock();				
				Thread.sleep(delayBetweenRuns);
			} catch (InterruptedException e) {
				logger.log(Level.SEVERE, e.toString(), e);
				interrupted = true;
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Problem ending unused sessions", e);
			} finally {
				if (sessionManagementLock.isHeldByCurrentThread()) {
					sessionManagementLock.unlock();
				}
			}
			super.setLastActivityTime(System.currentTimeMillis());
		}
		logger.severe("\n\nSessionReaper has finished. keepRunning is " + keepRunning + " ShutdownThreadIsRunning is " + allSessionPools.isShutdownThreadIsRunning() + " interrupted is " + interrupted);
	}

	public void setCloseMillis(long closeMillis) {
		this.closeMillis = closeMillis;
	}

	public void setLogoutMillis(long logoutMillis) {
		this.logoutMillis = logoutMillis;
	}
	
	@Override
	void setDelayBetweenRuns(long delayBetweenRuns) {
		this.delayBetweenRuns = delayBetweenRuns;
	}

	@Override
	long getDelayBetweenRuns() {
		return delayBetweenRuns;
	}
	
	@Override
	void setDelayBeforeRunning(long delayBeforeRunning) {
		this.delayBeforeRunning = delayBeforeRunning;
	}		
}
