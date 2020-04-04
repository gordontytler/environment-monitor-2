package monitor.model;



public class Action {

	private String outputName;
	private String commandLine;
	private String applicationNameInEnvironmentView;
	private boolean selectedByDefault = false;
	private boolean isCommand = false;
	
	public Action() {
	}
	
	public Action(String outputName, String commandLine, String applicationNameInEnvironmentView) {
		this.outputName = outputName;
		this.commandLine = commandLine;
		this.applicationNameInEnvironmentView = applicationNameInEnvironmentView;
	}

	public String getOutputName() {
		return outputName;
	}
	public void setOutputName(String outputName) {
		this.outputName = outputName;
	}
	public String getCommandLine() {
		return commandLine;
	}
	public void setScriptFile(String commandLine) {
		this.commandLine = commandLine;
	}

	public String getApplicationNameInEnvironmentView() {
		return applicationNameInEnvironmentView;
	}	
	
	public boolean isSelectedByDefault() {
		return selectedByDefault;
	}

	public void setSelectedByDefault(boolean selectedByDefault) {
		this.selectedByDefault = selectedByDefault;
	}

	/** A command does not produce continuous output and has no tickbox on the menu.
	 * It uses a TERMINAL session instead of an ACTION session. */
	public boolean isCommand() {
		return isCommand;
	}

	public void setCommand(boolean isCommand) {
		this.isCommand = isCommand;
	}

	@Override
	public String toString() {
		return "Action [application=" + applicationNameInEnvironmentView + ", outputName="
				+ outputName + ", commandLine=" + commandLine + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((outputName == null) ? 0 : outputName.hashCode());
		result = prime * result
				+ ((commandLine == null) ? 0 : commandLine.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Action other = (Action) obj;
		if (applicationNameInEnvironmentView == null) {
			if (other.applicationNameInEnvironmentView != null)
				return false;
		} else if (!applicationNameInEnvironmentView.equals(other.applicationNameInEnvironmentView))
			return false;
		if (outputName == null) {
			if (other.outputName != null)
				return false;
		} else if (!outputName.equals(other.outputName))
			return false;
		return true;
	}

	
}
