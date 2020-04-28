package monitor.model;

public class LogonResult {

	CommandStatus commandStatus;
	String sessionId = "";
	int bashProcessId;
	
	String errorMessage = "";
	
	public CommandStatus getCommandStatus() {
		return commandStatus;
	}
	public void setCommandStatus(CommandStatus commandStatus) {
		this.commandStatus = commandStatus;
	}
	public String getSessionId() {
		return sessionId;
	}
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	public int getBashProcessId() {
		return bashProcessId;
	}
	public void setBashProcessId(int bashProcessId) {
		this.bashProcessId = bashProcessId;
	}
}
