package monitor.model;

public class OutputInfo {

	private CommandStatus commandStatus;
	private int highestChunk;
	private int lowestChunk;
	private String sessionId;

	public OutputInfo() {
		super();
	}

	public CommandStatus getCommandStatus() {
		return commandStatus;
	}

	public void setCommandStatus(CommandStatus commandStatus) {
		this.commandStatus = commandStatus;
	}

	public int getHighestChunk() {
		return highestChunk;
	}

	public void setHighestChunk(int highestChunk) {
		this.highestChunk = highestChunk;
	}

	public int getLowestChunk() {
		return lowestChunk;
	}

	public void setLowestChunk(int lowestChunk) {
		this.lowestChunk = lowestChunk;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

}