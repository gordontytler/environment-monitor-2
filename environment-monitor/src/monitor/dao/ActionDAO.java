package monitor.dao;

import java.util.logging.Level;
import java.util.logging.Logger;

import monitor.implementation.MonitorRuntimeException;
import monitor.model.Action;
import monitor.model.Application;
import monitor.model.EnvironmentView;
import monitor.model.EnvironmentViewRow;

public class ActionDAO {

	static Logger logger = Logger.getLogger(ActionDAO.class.getName());	
	private EnvironmentViewDAO environmentViewDAO = EnvironmentViewDAO.getInstance();
	private ApplicationCache applicationCache = ApplicationCache.getInstance();	

	/** go to environment file to find the application file and in the application file we get the script */
	public Action loadAction(String environmentName, EnvironmentViewRow row) {
		Action action = null;
		EnvironmentView reloadedView = environmentViewDAO.loadUnsafeEnvironmentView(environmentName);
		
		// The first if is code done in a hurry for a dynamic action typed into the gui and not in the config
		if (row.getOutputName().startsWith("Execute...")) {
			String commandLine = row.getOutputName().substring("execute...".length());
			action = new Action(row.getOutputName(), commandLine, null);
		} else {
			// is it a server action such as Discover Apps
			if ("".equals(row.getApplicationName())) {
				String propertyKey = FileDAOHelper.toFileName(row.getOutputName());
				String commandLine = reloadedView.getProperties().get(propertyKey);
				if (commandLine == null) {
					throw new MonitorRuntimeException(String.format("Did not find '%s = scripts/<filename> in %s", propertyKey, reloadedView.getFileName()));
				}
				action = new Action(row.getOutputName(), commandLine, null);
			} else {
				// then its an action for a particular application, don't test for same output name in reloaded view because we may be adding it.
				Application application = null;			
				for (EnvironmentViewRow reloadedRow : reloadedView.getRows()) {
					if (row.getServerName().equals(reloadedRow.getServerName()) &&
						row.getApplicationName().equals(reloadedRow.getApplicationName()) ) {
						try {
							application = applicationCache.loadApplicationByFileName(reloadedRow.getApplication().getFileName(), row.getApplicationName());
							break;
						} catch (Exception e) {
							logger.log(Level.SEVERE, String.format("Problem loading application definition for serverName: %s applicationName: \"%s\" outputName: \"%s\" in %s ", row.getServerName(), row.getApplicationName(), row.getOutputName(), reloadedView.getFileName()), e);
						}
					}
				}
				if (application != null) {
					// now find the script file
					action = application.getActionByOutputName(row.getOutputName());
					if (action == null) {
						throw new MonitorRuntimeException(String.format("Did not find script file name for output '%s' in %s", row.getOutputName(), application.getFileName()));
					}
				}
			}
		}
		if (action == null) {
			throw new MonitorRuntimeException(String.format("Did not find row with serverName: %s applicationName: \"%s\" outputName: \"%s\" in %s ", row.getServerName(), row.getApplicationName(), row.getOutputName(), reloadedView.getFileName()));
		}
		return action;
	}


}
