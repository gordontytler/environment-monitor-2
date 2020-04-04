package monitor.implementation.application;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import monitor.dao.ApplicationCache;
import monitor.dao.ApplicationFileNamesDAO;
import monitor.dao.EnvironmentViewDAO;
import monitor.implementation.session.AllSessionPools;
import monitor.implementation.session.Session;
import monitor.model.Application;
import monitor.model.EnvironmentView;
import monitor.model.EnvironmentViewRow;

public class ApplicationViewBuilder {

	static Logger logger = Logger.getLogger(ApplicationViewBuilder.class.getName());	
	
	EnvironmentViewDAO environmentViewDAO = EnvironmentViewDAO.getInstance();
	ApplicationFileNamesDAO applicationFileNamesDAO = ApplicationFileNamesDAO.getInstance();
	ApplicationCache applicationCache = ApplicationCache.getInstance();
	private AllSessionPools allSessionPools = AllSessionPools.getInstance();

	/** Takes a comma separated list of sessionIds and returns the applications for each.
	 * Assumes all sessions are for the same environment. */
	public String getEnvironmentApplications(String sessions) {
		StringBuilder response = new StringBuilder();
		EnvironmentView view = null;
		try {
			String[] sessionIds = sessions.split(",");
			for (int i=0; i < sessionIds.length; i++) {
				Session session = allSessionPools.getSessionUsingSessionId(sessionIds[i]);
				if (session == null) {
					response.append(String.format("sessionId %s not found.\n", sessionIds[i]));
				} else {
					if (view == null) {
						String environmentName = session.getEnvironmentName();
						if (environmentName == null || environmentName.length() == 0) {
							response.append(String.format("There is no environmentName associated with sessionId %s.\n", sessionIds[i]));
						} else {
							view = environmentViewDAO.loadUnsafeEnvironmentView(environmentName);
							if (view == null) {
								response.append(String.format("sessionId %s has environmentName %s but view was not found in cache.\n", sessionIds[i], environmentName));
							}
						}
					}
					if (view != null) {
						String serverName = session.getServer().getHost();
						response.append(getServerApplications(view, serverName, sessionIds[i]));
					}
				}
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "problem in getEnvironmentApplications", e);
			response.append("Exception: " + e.getMessage() + "\n");
		}
		String responseString = response.toString();
		int lastDelimiter = responseString.lastIndexOf("\n<\n");
		if (lastDelimiter > 0) {
			return responseString.substring(0, lastDelimiter);
		} else {
			if (responseString.length() == 0) {
				return "No applications found for sessions: " + sessions;
			} else {
				return responseString;	
			}
			
		}
	}

	
	private String getServerApplications(EnvironmentView view, String serverName, String sessionId) {
		StringBuilder response = new StringBuilder();
		List<String> applictionNames = new ArrayList<String>();
		for (EnvironmentViewRow row : view.getRows()) {
			if (row.getServerName().equals(serverName) && row.getApplicationName().length() > 0) {
				Application applicationInRow = row.getApplication();
				if (!applictionNames.contains(applicationInRow.getNameInEnvironmentView())) {
					Application application = applicationCache.loadApplicationByFileName(applicationInRow.getFileName(), applicationInRow.getNameInEnvironmentView());
					application.setNameInEnvironmentView(applicationInRow.getNameInEnvironmentView());
					response.append(print(application, sessionId));
					response.append("<\n");  // python will split the output by '\n<\n'
					applictionNames.add(application.getNameInEnvironmentView());
				}
			}
		}
		return response.toString();
	}
	
	
	private String print(Application application, String sessionId) {
		StringBuilder response = new StringBuilder();
		response.append("sessionID............: ").append(sessionId).append('\n'); // place holder for sessionID
		response.append("nameInEnvironmentView: ").append(application.getNameInEnvironmentView()).append('\n');
		response.append("fileName.............: ").append(application.getFileName()).append('\n');
		List<String> discoveryChecks = application.getDiscoveryChecks();
		for (int i=0; i < discoveryChecks.size(); i++) {
			if (i==0) {
				response.append("ps -ef | grep java...: ");
			} else {
				response.append("and stdout '1' from..: ");
			}
			String discoveryCheck = environmentViewDAO.substituteVariables(discoveryChecks.get(i), application.getNameInEnvironmentView());
			response.append(discoveryCheck).append('\n');
		}
		return response.toString();
	}

	public String getAllApplications() {
		String nameInEnvironmentView = "";
		StringBuilder response = new StringBuilder();
		List<String> applicationFileNames = applicationFileNamesDAO.getApplicationFileNames();
		for (int i=0; i < applicationFileNames.size(); i++) {
			String fileName = applicationFileNames.get(i);
			Application application;
			try {
				application = applicationCache.loadApplicationByFileName(fileName, nameInEnvironmentView);
				application.setNameInEnvironmentView(application.getName());
				response.append(print(application, "0"));
				if (i < applicationFileNames.size() -1) {
					response.append("<\n");  // python will split the output by '\n<\n'					
				}
			} catch (Exception e) {
				logger.log(Level.SEVERE, "problem in getAllApplications", e);				
				response.append("Exception: " + e.getMessage() + "\n");
			}
		}
		return response.toString();
	}
	
}
