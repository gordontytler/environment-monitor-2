package monitor.implementation.action;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AlreadyRunningInfo {

	private String command;
	private Long timeStarted;
	private String sessionId;
	
	/** Used to check if an output is already running before starting another and to check that the correct processes are killed. */
	public AlreadyRunningInfo(String command, Long timeStarted, String sessionId) {
		super();
		this.command = command;
		this.timeStarted = timeStarted;
		this.sessionId = sessionId;
	}
	
	public String getCommand() {
		return command;
	}
	public void setCommand(String command) {
		this.command = command;
	}
	public Long getTimeStarted() {
		return timeStarted;
	}
	public void setTimeStarted(Long timeStarted) {
		this.timeStarted = timeStarted;
	}
	public String getSessionId() {
		return sessionId;
	}
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	@Override
	public String toString() {
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
		Date started = new Date(timeStarted);
		return String.format("sessionId:%-6s %s %s", sessionId, sdf.format(started), command);
	}
	
}
