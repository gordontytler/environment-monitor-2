package monitor.implementation.session;

import monitor.model.SessionType;

public class SessionEvent {

	private long time;
	private String shortDesc;
	private String sessionID;

	private boolean isLoggedOn;
	private boolean isOpen;
	private boolean isControlSession;
	private SessionType sessionType;
	private long lastUsed;
	
	private String longDesc;

	
	
	public SessionEvent(long time, String shortDesc, String sessionID,
			boolean isLoggedOn, boolean isOpen, boolean isControlSession,
			SessionType sessionType, String longDesc, long lastUsed) {
		this.time = time;
		this.shortDesc = shortDesc;
		this.sessionID = sessionID;
		this.isLoggedOn = isLoggedOn;
		this.isOpen = isOpen;
		this.isControlSession = isControlSession;
		this.sessionType = sessionType;
		this.longDesc = longDesc;
		this.lastUsed = lastUsed;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public String getShortDesc() {
		return shortDesc;
	}

	public void setShortDesc(String shortDesc) {
		this.shortDesc = shortDesc;
	}

	public String getSessionID() {
		return sessionID;
	}

	public void setSessionID(String sessionID) {
		this.sessionID = sessionID;
	}

	public boolean isLoggedOn() {
		return isLoggedOn;
	}

	public void setLoggedOn(boolean isLoggedOn) {
		this.isLoggedOn = isLoggedOn;
	}

	public boolean isOpen() {
		return isOpen;
	}

	public void setOpen(boolean isOpen) {
		this.isOpen = isOpen;
	}

	public boolean isControlSession() {
		return isControlSession;
	}

	public void setControlSession(boolean isControlSession) {
		this.isControlSession = isControlSession;
	}

	public SessionType getSessionType() {
		return sessionType;
	}

	public void setSessionType(SessionType sessionType) {
		this.sessionType = sessionType;
	}

	public String getLongDesc() {
		return longDesc;
	}

	public void setLongDesc(String longDesc) {
		this.longDesc = longDesc;
	}
	
	public long getLastUsed() {
		return lastUsed;
	}

	public void setLastUsed(long lastUsed) {
		this.lastUsed = lastUsed;
	}

	@Override
	public String toString() {
		return "SessionEvent [isControlSession=" + isControlSession
				+ ", isLoggedOn=" + isLoggedOn + ", isOpen=" + isOpen
				+ ", longDesc=" + longDesc + ", sessionID=" + sessionID
				+ ", sessionType=" + sessionType + ", shortDesc=" + shortDesc
				+ ", time=" + time + "]";
	}
	
	
}
