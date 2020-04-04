package monitor.implementation.environment;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import monitor.dao.EnvironmentViewDAO;
import monitor.implementation.action.ActionRunnable;
import monitor.model.Application;
import monitor.model.Configuration;
import monitor.model.EnvironmentView;
import monitor.model.EnvironmentViewRow;
import monitor.model.UpDownState;

public class RunningApplicationChecker {
	
	static Logger logger = Logger.getLogger(RunningApplicationChecker.class.getName());
	private static final boolean logFine = Configuration.getInstance().isLogFine();	

	private ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
	private EnvironmentViewDAO environmentViewDAO = EnvironmentViewDAO.getInstance();
	int applicationHeartbeatMillis = Configuration.getInstance().getApplicationHeartbeatMillis();
	Map<String, Long> lastCheckTimes = new HashMap<String, Long>();
	
	public synchronized void checkIfApplicationsAreRunning(EnvironmentView view) {
		String environmentName = view.getEnvironmentName();
		Long lastCheckTime = lastCheckTimes.get(environmentName);
		if (lastCheckTime != null && (new Date().getTime()) - lastCheckTime < applicationHeartbeatMillis) {
			return;
		}
		if (lastCheckTime == null) {
			logger.info("First run of RunningApplicationChecker for " + environmentName + ". lastCheckTime is null. applicationHeartbeatMillis: " + applicationHeartbeatMillis);
		}
		StringBuilder sb = new StringBuilder();
		ArrayList<String> namesInEnvironmentView = new ArrayList<String>();
		for (EnvironmentViewRow row : view.getRows()) {
			Application application = row.getApplication();
			
			if (application != null && application.getNameInEnvironmentView().length() > 0) {
				String nameInEnvironmentView = application.getNameInEnvironmentView();
				if (!namesInEnvironmentView.contains(nameInEnvironmentView)) {
					namesInEnvironmentView.add(nameInEnvironmentView);
				}
				// TODO An application can have several output rows. We should not need to change its UpDownState for each row
				// but actually each row has a separate application instance.
				if (lastCheckTime == null) {
					application.setUpDownState(UpDownState.UNKNOWN);
					if (!namesInEnvironmentView.contains(nameInEnvironmentView)) {
						sb.append(nameInEnvironmentView + " is " + application.getUpDownState() + ", ");
					}
				} else {
					// we have already checked so those still UNKNOWN were not set to UP on previous check so must be DOWN
					// but this wont happen if the python script calls finishedHeatbeat
					if (application.getUpDownState() == UpDownState.UNKNOWN) {
						application.setUpDownState(UpDownState.DOWN);
						if (!namesInEnvironmentView.contains(nameInEnvironmentView)) {						
							sb.append(nameInEnvironmentView + " is " + application.getUpDownState() + ", ");
						}
					} else if (application.getUpDownState() == UpDownState.UP){
						application.setUpDownState(UpDownState.UNKNOWN);
						if (!namesInEnvironmentView.contains(nameInEnvironmentView)) {
							sb.append(nameInEnvironmentView + " was UP,  ");
						}
					} else {
						if (!namesInEnvironmentView.contains(nameInEnvironmentView)) {
							sb.append(nameInEnvironmentView + " is " + application.getUpDownState() + ", ");
						}
					}
				}
			}
		}
		lastCheckTimes.put(view.getEnvironmentName(), new Long(new Date().getTime()));
		if (namesInEnvironmentView.size() > 0) {
			if (logFine) logger.info(String.format("'%s'\n\n\n\n%s ", environmentName, sb.toString()));
			EnvironmentViewRow heartbeat = new EnvironmentViewRow();
			heartbeat.runsOnAllServers();
			// the name of the script file to run is in the environment file 
			heartbeat.setOutputName("Apps Heartbeat");
			heartbeat.setApplicationName("");
			// This will execute scripts/appsHeartbeatOnAllServers001.py which calls scripts/discoverApps001.py with heartbeat parameter
			singleThreadExecutor.execute(new ActionRunnable(view.getEnvironmentName(), heartbeat));
		}
	}

	public String finishedHeartbeat(String environmentName, String host) {
		StringBuilder sb = new StringBuilder();
		ArrayList<String> downApplications = new ArrayList<String>();
		String result = null;
		EnvironmentView view = environmentViewDAO.loadUnsafeEnvironmentView(environmentName);
		for (EnvironmentViewRow row : view.getRows()) {
			if (host.equals(row.getServerName())) {
				Application application = row.getApplication();
				if (application.getUpDownState() == UpDownState.UNKNOWN) {
					application.setUpDownState(UpDownState.DOWN);
					if (!downApplications.contains(row.getApplicationName())) {
						downApplications.add(row.getApplicationName());
						sb.append(row.getApplicationName() + ", ");
					}					
				}
			}
		}
		if (sb.length() > 0) {
			result = "These applications are now DOWN: " + sb.toString();
		} else {
			result = "No change to DOWN appliations "; 
		}
		return result; 
	}

}
