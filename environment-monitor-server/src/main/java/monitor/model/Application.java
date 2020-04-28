package monitor.model;

import java.util.HashMap;
import java.util.List;

import monitor.implementation.MonitorRuntimeException;

public class Application {

	private String name;
	private String nameInEnvironmentView;
	private String fileName;
	private List<String> discoveryChecks;
	private List<Action> actions;
	private HashMap<String, Action> actionMap = null;
	private UpDownState upDownState = UpDownState.UNKNOWN;
	
	public Application() {
	}
	
	public Application(String nameInEnvironmentView, String fileName) {
		this.nameInEnvironmentView = nameInEnvironmentView;
		this.fileName = fileName;
	}
	
	/** @Return a copy which allows the nameInEnvironmentView to be changed without affecting the cached application.
	 * All other attributes are shared with the cached application. */
	public Application getSafeCopy() {
		Application copy = new Application(nameInEnvironmentView, fileName);
		copy.setName(name);
		copy.setDiscoveryChecks(discoveryChecks);
		copy.setActions(actions);
		copy.setUpDownState(upDownState);
		return copy;
	}
	
	/** The name of one instance of the application on a server. */
	public String getNameInEnvironmentView() {
		return nameInEnvironmentView;
	}

	public void setNameInEnvironmentView(String nameInEnvironmentView) {
		this.nameInEnvironmentView = nameInEnvironmentView;
	}
	/** The name of the meta data describing the application which is stored in fileName file. Several applications with different
	 *  nameInEnvironmentView can be described by the same file. 
	 *
	 *  This explains why we need a separate fileName but not why we need name and nameInEnvironmentView.
	 *  The file which defines the environment has a name for the application. This is nameInEnvironmentView.
	 *  The file which defines the application can have a different name. 
	 **/
	public String getName() {
		return name; // which defines the application
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public List<Action> getActions() {
		return actions;
	}

	public void setActions(List<Action> actions) {
		this.actions = actions;
		actionMap = new HashMap<String, Action>(actions.size());
		for (Action action : actions) {
			if (actionMap.get(action.getOutputName()) != null) {
				throw new MonitorRuntimeException(String.format("duplicate outputName '%s' in file %s", action.getOutputName(), this.getFileName()));
			}
			actionMap.put(action.getOutputName(), action);
			if (!(this.nameInEnvironmentView.equals(action.getApplicationNameInEnvironmentView()))) {
				throw new MonitorRuntimeException(String.format("The action '%s' can not be added to '%s' application because it already references '%s' application.",
						action, this, action.getApplicationNameInEnvironmentView()));
			}
		}
	}

	public Action getActionByOutputName(String outputName) {
		return actionMap.get(outputName);
	}

	public UpDownState getUpDownState() {
		return upDownState;
	}

	public void setUpDownState(UpDownState upDownState) {
		this.upDownState = upDownState;
	}

	/** 
	 * The first element is text in the output from ps -ef | grep java, that must be present when application running.
	 * If there are more elements these contain commands that all return 1 when the application is present.
	 * @return
	 */
	public List<String> getDiscoveryChecks() {
		return discoveryChecks;
	}

	public void setDiscoveryChecks(List<String> discoveryChecks) {
		this.discoveryChecks = discoveryChecks;
	}

	@Override
	public String toString() {
		return "Application [fileName=" + fileName + ", nameInEnvironmentView=" + nameInEnvironmentView + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		//result = prime * result + ((actionMap == null) ? 0 : actionMap.hashCode());
		result = prime * result + ((actions == null) ? 0 : actions.hashCode());
		result = prime * result + ((discoveryChecks == null) ? 0 : discoveryChecks.hashCode());
		result = prime * result + ((fileName == null) ? 0 : fileName.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((nameInEnvironmentView == null) ? 0 : nameInEnvironmentView.hashCode());
		result = prime * result + ((upDownState == null) ? 0 : upDownState.hashCode());
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
		Application other = (Application) obj;
		
		//if (actionMap == null) {
		//	if (other.actionMap != null)
		//		return false;
		//} else if (!actionMap.equals(other.actionMap))
		//	return false;
		
		if (actions == null) {
			if (other.actions != null)
				return false;
		} else if (!actions.equals(other.actions))
			return false;
		if (discoveryChecks == null) {
			if (other.discoveryChecks != null)
				return false;
		} else if (!discoveryChecks.equals(other.discoveryChecks))
			return false;
		if (fileName == null) {
			if (other.fileName != null)
				return false;
		} else if (!fileName.equals(other.fileName))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (nameInEnvironmentView == null) {
			if (other.nameInEnvironmentView != null)
				return false;
		} else if (!nameInEnvironmentView.equals(other.nameInEnvironmentView))
			return false;
		if (upDownState == null) {
			if (other.upDownState != null)
				return false;
		} else if (!upDownState.equals(other.upDownState))
			return false;
		return true;
	}
	
}
