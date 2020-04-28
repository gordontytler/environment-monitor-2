package monitor.dao;

import java.util.ArrayList;
import java.util.List;

import monitor.implementation.session.AllSessionPools;
import monitor.implementation.session.Session;
import monitor.model.Command;
import monitor.model.CommandResult;
import monitor.model.Configuration;

public class EnvironmentNamesDAO {

	private static EnvironmentNamesDAO theInstance = new EnvironmentNamesDAO();
	private final String fullPathToInstallDirectory = Configuration.getInstance().getFullPathToInstallDirectory();
	private AllSessionPools allSessionPools = AllSessionPools.getInstance();
	EnvironmentViewDAO environmentViewDAO = EnvironmentViewDAO.getInstance();
	ArrayList<String> names = new ArrayList<String>();
	
	private EnvironmentNamesDAO() {
	}

	public static EnvironmentNamesDAO getInstance() {
		return theInstance;
	}
	
	public synchronized List<String> getEnvironmentNames() {
		if (names.size() == 0) {
			Session session = allSessionPools.getLocalSession("->EnvironmentNamesDAO.getEnvironmentNames");
			// get the names from files containing "environmentName = " 
			Command command = new Command("grep environmentName " + fullPathToInstallDirectory + "/" + DataDirectory.getDataDirectory() + "/environments/* | awk -F'= ' '{print $2}'");
			CommandResult commandResult = session.executeCommand(command);
			session.close("getEnvironmentNames");
			String[] lines = commandResult.getOutput().split("\n");
			for (String line : lines) {
				if (line.trim().length() > 0) {
					names.add(line.trim());
				}
			}
		}
		// add any environments that have yet to be saved
		for (String cachedName : environmentViewDAO.getCachedNames()) {
			if (!names.contains(cachedName)) {
				names.add(cachedName);
			}
		}
		return names;
	}

	public synchronized void resetCache() {
		names = new ArrayList<String>();
	}
	
	
}
