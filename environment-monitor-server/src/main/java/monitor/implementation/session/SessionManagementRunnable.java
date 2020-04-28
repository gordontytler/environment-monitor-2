package monitor.implementation.session;

/** A Runnable that can be monitored by SessionManager and interrupted if it gets stuck. */
abstract class SessionManagementRunnable implements Runnable {
	
	long timeLastRan = System.currentTimeMillis();
	long maxExpectedDelayBetweenRuns;
	long runCount = 0;
	boolean keepRunning = true;
	
	void stop() {
		keepRunning = false;
	}
	
	long getTimeLastRan() {
		return timeLastRan;
	}
	void setLastActivityTime(long timeLastRan) {
		this.timeLastRan = timeLastRan;
		runCount++;
	}
	long getMaxExpectedDelayBetweenRuns() {
		return maxExpectedDelayBetweenRuns;
	}
	void setMaxExpectedDelayBetweenRuns(long maxExpectedDelayBetweenRuns) {
		this.maxExpectedDelayBetweenRuns = maxExpectedDelayBetweenRuns;
	}
	
	
	// for tests	
	public long getRunCount() {
		return runCount;
	}
	public void setRunCount(long runCount) {
		this.runCount = runCount;
	}
	
	abstract void setDelayBetweenRuns(long delayBetweenRuns);

	abstract void setDelayBeforeRunning(long delayBeforeRunning);

	abstract long getDelayBetweenRuns();

	
}
