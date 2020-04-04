package monitor.implementation.environment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import monitor.dao.ApplicationCache;
import monitor.dao.ApplicationFileNamesDAO;
import monitor.dao.EnvironmentNamesDAO;
import monitor.dao.EnvironmentViewDAO;
import monitor.implementation.action.ActionRunnable;
import monitor.implementation.session.AllSessionPools;
import monitor.implementation.session.ServerSessionPool;
import monitor.implementation.session.Session;
import monitor.implementation.shell.ChunkedOutput;
import monitor.model.Application;
import monitor.model.CommandStatus;
import monitor.model.Configuration;
import monitor.model.EnvironmentView;
import monitor.model.EnvironmentViewRow;
import monitor.model.OutputHistory;
import monitor.model.Server;
import monitor.model.SessionType;

/**
 * An environment view is a list of servers, applications and pre-defined outputs.
 * <p> 
 * Ad-hoc (not pre-defined) continuous output from a terminal session or output from a running action is added at the end.
 * 
 * e.g.
 * <table border="1">
 * 	<tr> <td>server one</td>&nbsp<td>&nbsp</td>&nbsp<td>&nbsp</td>&nbsp<td>&nbsp</td> 	</tr>
 * 	<tr> <td>server two</td><td>application one</td><td>request.log</td><td>Not running</td>  </tr>
 * 	<tr> <td>server two</td><td>application one</td><td>error.log</td><td>Running</td>  </tr>
 *  <tr> <td>server two</td><td>&nbsp</td><td>tail -f somefile</td><td>Running</td>  </tr> 
 *</table>
 * 
 * @param environmentName
 * @return
 */
public class EnvironmentViewBuilder {
	
	static Logger logger = Logger.getLogger(EnvironmentViewBuilder.class.getName());
	private String environmentName;
	private static final int MAX_HISTORY = Configuration.getInstance().getChunkedOutputArraySize();
	private static final int RESTART_DELAY_PER_ROW_MILLIS = 5000;
	private EnvironmentViewDAO serverDAO = EnvironmentViewDAO.getInstance();
	private RunningApplicationChecker runningApplicationChecker = new RunningApplicationChecker();
	private AllSessionPools allSessionPools = AllSessionPools.getInstance();
	private ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
	private boolean environmentBeingRestarted = false;
	private long environmentRestartedTime = 0l;
	private List<String> serversBeingRestarted = new ArrayList<String>();	
	private static HashMap<String, EnvironmentViewBuilder> instances = new HashMap<String, EnvironmentViewBuilder>();	
	
	private EnvironmentViewBuilder(String environmentName) {
		this.environmentName = environmentName;
	}
	
	/** There is a separate instance for each environment so that they each get their own singleThreadExecuter. */
	public synchronized static EnvironmentViewBuilder getInstance(String environmentName) {
		EnvironmentViewBuilder anInstance = instances.get(environmentName);
		if (anInstance == null) {
			anInstance = new EnvironmentViewBuilder(environmentName);
			instances.put(environmentName, anInstance);
		}
		return anInstance;
	}
	
	public synchronized EnvironmentView getEnvironmentView(long oldestHistoryTimeStamp) {

		EnvironmentView staticView = serverDAO.loadEnvironmentView(environmentName);
		List<EnvironmentViewRow> rows = staticView.getRows(); 
		if (rows.size() == 0 || allSessionPools.isShutdownThreadIsRunning()) {
			return staticView;
		}
		if (!environmentBeingRestarted) {
			runningApplicationChecker.checkIfApplicationsAreRunning(staticView);
		}
		List<EnvironmentViewRow> newRows = new ArrayList<EnvironmentViewRow>();
		String currentServer = rows.get(0).getServerName();
		List<Session> preDefinedActionSessions = getActionSessions(currentServer, false);

		long nextOutputHistoryTimeStamp = System.nanoTime();		

		// for each server find the running actions
		for (int r=0; r < rows.size(); r++) {
			EnvironmentViewRow row = rows.get(r);
			
			// last row or different server
			if (!currentServer.equals(row.getServerName()) || r ==  rows.size() -1) {
				// if last row and different server
				if (r ==  rows.size() -1 && !currentServer.equals(row.getServerName())) {
					// add the running commands for the previous server
					addAdhocRunningCommands(newRows, currentServer, oldestHistoryTimeStamp);
					// add the last server
					preDefinedActionSessions = getActionSessions(row.getServerName(), false);
					addPreDefinedActionRow(newRows, preDefinedActionSessions, row, oldestHistoryTimeStamp, environmentName);
					// add the running commands for the last server
					addAdhocRunningCommands(newRows, row.getServerName(), oldestHistoryTimeStamp);
				// last row same server
				} else if (r ==  rows.size() -1) {
					addPreDefinedActionRow(newRows, preDefinedActionSessions, row, oldestHistoryTimeStamp, environmentName);
					// add the running commands for the last server
					addAdhocRunningCommands(newRows, row.getServerName(), oldestHistoryTimeStamp);
				// must be different server not last row
				} else {
					// add the running commands for the previous server
					addAdhocRunningCommands(newRows, currentServer, oldestHistoryTimeStamp);
					// add the new server
					preDefinedActionSessions = getActionSessions(row.getServerName(), false);
					addPreDefinedActionRow(newRows, preDefinedActionSessions, row, oldestHistoryTimeStamp, environmentName);
				}
				currentServer = row.getServerName();
			// not last row and same server
			} else {
				addPreDefinedActionRow(newRows, preDefinedActionSessions, row, oldestHistoryTimeStamp, environmentName);
			}
		}
		EnvironmentView viewWithActivity = new EnvironmentView(environmentName);
		viewWithActivity.setRows(newRows);
		viewWithActivity.setOutputHistoryTimeStamp(nextOutputHistoryTimeStamp);
		return viewWithActivity;
	}

	/** If an outputName is defined find the corresponding session and attach its output history.
	 * If session is not found attempt to start the output. */
	private void addPreDefinedActionRow(List<EnvironmentViewRow> newRows, List<Session> monitorSessions, EnvironmentViewRow row, long oldestHistoryTimeStamp, String environmentName) {
		if (row.getOutputName() == null || row.getOutputName().trim().length() == 0 || serversBeingRestarted.contains(row.getServerName())) {
			newRows.add(row);			
			return;
		}
		boolean foundRunningOutput = false;
		for (Session monitorSession : monitorSessions) {
			if (row.getOutputName().equals(monitorSession.getAction().getOutputName())) {
				// can't have two applications with the same name on the same server
				if (row.getApplication().getNameInEnvironmentView().equals(monitorSession.getAction().getApplicationNameInEnvironmentView())) {
					row.setCommandStatus(CommandStatus.RUNNING);
					row.setHighestChunk(monitorSession.getChunkedOutput().getHighestChunkNumber());
					row.setLowestChunk(monitorSession.getChunkedOutput().getLowestChunkNumber());
					row.setSessionId(monitorSession.getSessionId());
					row.setOutputHistory(makeOutputHistory(monitorSession.getChunkedOutput(), oldestHistoryTimeStamp));
					foundRunningOutput = true;
					((ExtendedRow)row).setOutputStartupAttempted(false);
					break;
				}
			}
		}
		if (!foundRunningOutput && !((ExtendedRow)row).isOutputStartupAttempted()) {
			((ExtendedRow)row).setOutputStartupAttempted(true);
			singleThreadExecutor.execute(new ActionRunnable(environmentName, row));
		}
		newRows.add(row);
	}


	private void addAdhocRunningCommands(List<EnvironmentViewRow> newRows, String currentServer, long oldestHistoryTimeStamp) {
		ServerSessionPool ssp = allSessionPools.getServerSessionPool(new Server(currentServer)); 
		List<Session> sessions = ssp.getSessionsByType(false, SessionType.TERMINAL);
		for (Session session : sessions) {
			EnvironmentViewRow newRow = new EnvironmentViewRow(currentServer, new Application("",""), session.getRunningCommand());
			newRow.setCommandStatus(CommandStatus.RUNNING);
			newRow.setHighestChunk(session.getChunkedOutput().getHighestChunkNumber());
			newRow.setLowestChunk(session.getChunkedOutput().getLowestChunkNumber());
			newRow.setSessionId(session.getSessionId());
			newRow.setOutputHistory(makeOutputHistory(session.getChunkedOutput(), oldestHistoryTimeStamp));
			newRows.add(newRow);
		}
	}
	
	
	private List<OutputHistory> makeOutputHistory(ChunkedOutput chunkedOutput, long oldestHistoryTimeStamp) {
		ArrayList<OutputHistory> outputHistories = new ArrayList<OutputHistory>();
		
		int size = chunkedOutput.getHighestChunkNumber() - chunkedOutput.getLowestChunkNumber();
		if (size > MAX_HISTORY) {
			size = MAX_HISTORY;
		}
		
		int x = chunkedOutput.getHighestChunkNumber();
		if (x > -1) {
			long timeStamp = chunkedOutput.getTimeStamp(x);
			int count = 0;
			
			while (timeStamp > oldestHistoryTimeStamp && count < size) {
				outputHistories.add(chunkedOutput.getOutputHistory(x--));
				timeStamp = chunkedOutput.getTimeStamp(x);
				count++;
			}
		}
		return outputHistories;
	}

	/** 
	 * Saves the environment. Flushes the caches of applications and environment names. Stops all running output.
	 * Removes the environment from the cache. The next call to MonitorServiceImpl.getEnvironmentView will restart the outputs. 
	 **/
	public void restartOutputs() {
		logger.info("\n\n\nrestartOutputs for " + environmentName);

		try {
			List<String> serverNames = serverDAO.getServerNames(environmentName);
			EnvironmentView cachedView = serverDAO.loadUnsafeEnvironmentView(environmentName);
			if (environmentRestartedTime > 0) {
				long timeRequired = cachedView.getRows().size() * RESTART_DELAY_PER_ROW_MILLIS;
				long timeElapsed = System.currentTimeMillis() - environmentRestartedTime; 
				if (timeElapsed < timeRequired) {
					logger.info(String.format("further restartOutputs requests for %s will be ignorred for the next %d millis.", environmentName, timeRequired - timeElapsed));
					return;
				}
			}
			environmentRestartedTime = System.currentTimeMillis();
			environmentBeingRestarted = true;
			
			if (cachedView.isRowsModified()) {
				serverDAO.saveEnvironmentView(environmentName);			
			}
			serverDAO.resetCache();
			EnvironmentNamesDAO.getInstance().resetCache();
			ApplicationFileNamesDAO.getInstance().resetCache();
			ApplicationCache.getInstance().resetCache();
			serverDAO.removeFromCache(environmentName);
			serversBeingRestarted = new ArrayList<String>(30);
			
			for (String serverName : serverNames) {
				serversBeingRestarted.add(serverName);
				List<Session> preDefinedActionSessions = getActionSessions(serverName, true);
				for (Session session : preDefinedActionSessions) {
					session.close("EnvironmentViewBuilder.restartOutputs");
				}
				serversBeingRestarted.remove(serverName);
			}
			allSessionPools.dumpAllSessionsOnAllServers("after closing all running or finished SessionType.ACTION sessions for " + environmentName);

		} catch (Exception e) {
			logger.log(Level.SEVERE, "restart outputs for " + environmentName, e);
		}
		environmentBeingRestarted = false;
	}
	
	public boolean isEnvironmentBeingRestarted() {
		return environmentBeingRestarted;
	}
	
	private List<Session> getActionSessions(String serverName, boolean includeFinished) {
		ServerSessionPool ssp = allSessionPools.getServerSessionPool(new Server(serverName)); 
		return ssp.getSessionsByType(includeFinished, SessionType.ACTION);
	}

	public void setServerDAO(EnvironmentViewDAO serverDAO) {
		this.serverDAO = serverDAO;
	}


	public void setAllSessionPools(AllSessionPools allSessionPools) {
		this.allSessionPools = allSessionPools;
	}

	public RunningApplicationChecker getRunningApplicationChecker() {
		return runningApplicationChecker;
	}

	public void setRunningApplicationChecker(RunningApplicationChecker runningApplicationChecker) {
		this.runningApplicationChecker = runningApplicationChecker;
	}
	
	

}
