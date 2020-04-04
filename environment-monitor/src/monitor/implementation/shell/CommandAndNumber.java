package monitor.implementation.shell;

import monitor.model.Command;

class CommandAndNumber {
	/** unblocks an ArrayBlockingQueue. The command is ignored. */
	public static final String WAKE_UP = "# wake up !";
	private int commandNumber;
	private Command command;
	public CommandAndNumber(int commandNumber, Command command) {
		this.commandNumber = commandNumber;
		this.command = command;
	}
	public int getCommandNumber() {
		return commandNumber;
	}
	public Command getCommand() {
		return command;
	}
	public String getRequest() {
		return command.getRequest();
	}
	
}