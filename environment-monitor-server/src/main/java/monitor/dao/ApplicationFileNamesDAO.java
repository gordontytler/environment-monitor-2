package monitor.dao;

import java.util.ArrayList;
import java.util.List;

import monitor.implementation.session.AllSessionPools;
import monitor.implementation.session.Session;
import monitor.model.Command;
import monitor.model.CommandResult;
import monitor.model.Configuration;

public class ApplicationFileNamesDAO {

	private static ApplicationFileNamesDAO theInstance = new ApplicationFileNamesDAO();
	private AllSessionPools allSessionPools = AllSessionPools.getInstance();

	ArrayList<String> names = new ArrayList<String>();
	
	private ApplicationFileNamesDAO() {
	}

	public static ApplicationFileNamesDAO getInstance() {
		return theInstance;
	}
	
	public synchronized List<String> getApplicationFileNames() {
		if (names.size() == 0) {
			Session session = allSessionPools.getLocalSession("->ApplicationFileNamesDAO.getApplicationFileNames");
			Command command = new Command("ls -1 " + Configuration.getInstance().getDataDirectory() + "applications/");
			CommandResult commandResult = session.executeCommand(command);
			session.close("getApplicationFileNames");
			String[] lines = commandResult.getOutput().split("\n");
			for (String line : lines) {
				names.add("applications/" + line.trim());
			}
		}
		return names;
	}

	public synchronized void resetCache() {
		names = new ArrayList<String>();
	}
	
	
}

