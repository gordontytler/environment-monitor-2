package monitor.model;

public class CommandResult {

	private CommandStatus commandStatus;
	private String output = "";
	private int chunkNumber;	
	private String sessionId;
	
	public static final CommandResult DEFAULT = new CommandResult(CommandStatus.FINISHED, "", 0, null);
	
	public CommandResult(CommandStatus commandStatus, String output,
			int chunkNumber, String sessionId) {
		super();
		this.commandStatus = commandStatus;
		this.output = output;
		this.chunkNumber = chunkNumber;
		this.sessionId = sessionId;
	}
	
	public CommandResult() {
	}

	public CommandStatus getCommandStatus() {
		return commandStatus;
	}
	public void setCommandStatus(CommandStatus commandStatus) {
		this.commandStatus = commandStatus;
	}
	public String getOutput() {
		return output;
	}
	public void setOutput(String output) {
		this.output = output;
	}
	public int getChunkNumber() {
		return chunkNumber;
	}
	public void setChunkNumber(int chunkNumber) {
		this.chunkNumber = chunkNumber;
	}
	public String getSessionId() {
		return sessionId;
	}
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	
}
