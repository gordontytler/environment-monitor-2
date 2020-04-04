package monitor.implementation.action;


public class AlreadyRunningKey {

	private String serverName; 
	private String nameInEnvironmentView; 
	private String outputName;

	public AlreadyRunningKey(String serverName, String nameInEnvironmentView, String outputName) {
		this.serverName = serverName;
		this.nameInEnvironmentView = nameInEnvironmentView;
		this.outputName = outputName;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public String getNameInEnvironmentView() {
		return nameInEnvironmentView;
	}

	public void setNameInEnvironmentView(String nameInEnvironmentView) {
		this.nameInEnvironmentView = nameInEnvironmentView;
	}

	public String getOutputName() {
		return outputName;
	}

	public void setOutputName(String outputName) {
		this.outputName = outputName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((nameInEnvironmentView == null) ? 0 : nameInEnvironmentView
						.hashCode());
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
		AlreadyRunningKey other = (AlreadyRunningKey) obj;
		if (nameInEnvironmentView == null) {
			if (other.nameInEnvironmentView != null)
				return false;
		} else if (!nameInEnvironmentView.equals(other.nameInEnvironmentView))
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
	
	@Override
	public String toString() {
		return String.format("%-35s %-20s %-15s", serverName, nameInEnvironmentView, outputName);
	}
	
}
