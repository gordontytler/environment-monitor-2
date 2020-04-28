package monitor.model;

import java.util.List;

public class EnvironmentViewRow extends OutputInfo {
	
	private String serverName;
	private Application application;
	private String outputName;
	private List<OutputHistory> outputHistory;
	public static final String ALL_SERVERS = "all servers"; 
	
	public EnvironmentViewRow() {
	}

	public EnvironmentViewRow(String serverName, Application application, String outputName) {
		super();
		this.serverName = serverName;
		this.application = application;
		this.outputName = outputName;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public void runsOnAllServers() {
		this.serverName = ALL_SERVERS;
	}
	
	/** @return empty String if no application or the application.getNameInEnvironmentView(). 
	 * See also {@link monitor.model.Application#getName()} */
	public String getApplicationName() {
		return application == null ? "" : application.getNameInEnvironmentView();
	}

	// setter needed to make application name a bean property published by the web service
	public void setApplicationName(String applicationName) {
		if (this.application != null) {
			application.setNameInEnvironmentView(applicationName);
		} else {
			application = new Application(applicationName, "");
		}
	}

	public Application getApplication() {
		return application;
	}

	public String getOutputName() {
		return outputName;
	}

	public void setOutputName(String outputName) {
		this.outputName = outputName;
	}
	
	public List<OutputHistory> getOutputHistory() {
		return outputHistory;
	}

	public void setOutputHistory(List<OutputHistory> outputHistory) {
		this.outputHistory = outputHistory;
	}

	public void setApplication(Application application) {
		this.application = application;
	}

	public String getActionDescription() {
		return String.format("%s on %s %s", outputName, serverName, getApplicationName());
	}
	
	@Override
	public String toString() {
		return "\nEnvironmentViewRow [application=" + application
				+ ", outputName=" + outputName + ", serverName=" + serverName
				+ "]";
	}

	/** The Environment view returned from EnvironmentViewBuilder is mutable and is the same object cached by EnvironmentViewDAO.
	 *  This makes it respond to changes. When reloading we need to blank anything that may have changed or is now out of date. */
	public void resetNonMetaDataFields() {
		//  dao does this :- rows.add(new EnvironmentViewRow(server, new Application(nameInEnvironmentView, applicationConfigFileName), outputName));
		// so here we reset everything else that may have changed - but what if something changes one of the file fields that I don't reset ?
		// and what if I reset some object that another cache has a reference to ?
		setCommandStatus(null);
		setHighestChunk(0);
		setLowestChunk(0);
		setSessionId("RESET"); // if client finds this it logs on again and restarts the output
		setOutputHistory(null);
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((application == null) ? 0 : application.hashCode());
		result = prime * result
				+ ((outputName == null) ? 0 : outputName.hashCode());
		result = prime * result
				+ ((serverName == null) ? 0 : serverName.hashCode());
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
		EnvironmentViewRow other = (EnvironmentViewRow) obj;
		if (application == null) {
			if (other.application != null)
				return false;
		} else if (!application.equals(other.application))
			return false;
		if (outputName == null) {
			if (other.outputName != null)
				return false;
		} else if (!outputName.equals(other.outputName))
			return false;
		if (serverName == null) {
			if (other.serverName != null)
				return false;
		} else if (!serverName.equals(other.serverName))
			return false;
		return true;
	}


	
}
