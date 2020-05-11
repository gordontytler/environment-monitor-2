package monitor.implementation.action;

import java.util.logging.Level;
import java.util.logging.Logger;

import monitor.implementation.session.AllSessionPools;
import monitor.model.Configuration;
import monitor.model.EnvironmentViewRow;

public class ActionRunnable implements Runnable {

	static Logger logger = Logger.getLogger(ActionRunnable.class.getName());
	static final boolean logFine = Configuration.getInstance().isLogFine();	
	
	private AllSessionPools allSessionPools = AllSessionPools.getInstance();	
	private ActionExecuter actionExecuter = new ActionExecuter();	
	
	String environmentName;
	EnvironmentViewRow row;
	
	public ActionRunnable(String environmentName, EnvironmentViewRow environmentViewRow) {
		super();
		this.environmentName = environmentName;
		this.row = environmentViewRow;
	}


	public void run() {
		if (logFine) logger.info("Begin executing " + row.getActionDescription());
		try {		
			actionExecuter.executeAction(environmentName, row);
			if (logFine) logger.info("Finished starting the excution of " + row.getActionDescription());			
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception excuting " + row.getActionDescription(), e);
			allSessionPools.dumpAllSessionsOnAllServers("there was an exception in ActionRunnable.run");
		}
	}

}
