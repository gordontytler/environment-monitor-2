package monitor.implementation.action;

import monitor.implementation.session.Session;

public class AlreadyRunningCheckerResult {

	private boolean shouldRun;
	private Session controlSession;
	
	public AlreadyRunningCheckerResult(boolean shouldRun) {
		this.shouldRun = shouldRun;
	}
	public boolean isShouldRun() {
		return shouldRun;
	}
	public void setShouldRun(boolean shouldRun) {
		this.shouldRun = shouldRun;
	}
	public Session getControlSession() {
		return controlSession;
	}
	public void setControlSession(Session controlSession) {
		this.controlSession = controlSession;
	}
	
	
	
}
