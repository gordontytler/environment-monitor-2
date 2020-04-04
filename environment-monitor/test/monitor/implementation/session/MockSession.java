package monitor.implementation.session;

import monitor.implementation.shell.ChunkedOutput;
import monitor.model.CommandStatus;

public class MockSession extends Session {

	CommandStatus lastCommandStatus;
	String runningCommand;
	String sessionId = new NextSessionId().makeNewSessionId();
	ChunkedOutput chunkedOutput = new ChunkedOutput(100);
	
	public MockSession() {
		open();
		loggedOn = true;
	}
	
	public void setLastCommandStatus(CommandStatus lastCommandStatus) {
		this.lastCommandStatus = lastCommandStatus;
	}
	
	@Override
	public ChunkedOutput getChunkedOutput() {
		lastUsed = System.currentTimeMillis();
		return this.chunkedOutput;
	}	

	@Override
	public String getSessionId() {
		return this.sessionId;
	}
	
	@Override
	public CommandStatus getLastCommandStatus() {
		return lastCommandStatus;
	}

	@Override
	public String getRunningCommand() {
		return runningCommand;
	}

	public void setRunningCommand(String runningCommand) {
		this.runningCommand = runningCommand;
	}
	
}
