package monitor.implementation.environment;

import monitor.model.Application;
import monitor.model.EnvironmentViewRow;

public class ExtendedRow extends EnvironmentViewRow {

	private boolean outputStartupAttempted;

	public ExtendedRow(String server, Application application, String outputName) {
		super(server, application, outputName);
	}

	public boolean isOutputStartupAttempted() {
		return outputStartupAttempted;
	}

	public void setOutputStartupAttempted(boolean outputStartupAttempted) {
		this.outputStartupAttempted = outputStartupAttempted;
	}
	
}
