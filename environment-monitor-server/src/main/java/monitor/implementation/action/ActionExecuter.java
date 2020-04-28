package monitor.implementation.action;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import monitor.dao.ActionDAO;
import monitor.implementation.session.AllSessionPools;
import monitor.implementation.session.ServerSessionPool;
import monitor.implementation.session.Session;
import monitor.model.Action;
import monitor.model.CommandResult;
import monitor.model.CommandStatus;
import monitor.model.Configuration;
import monitor.model.EnvironmentViewRow;
import monitor.model.Server;
import monitor.model.SessionType;

public class ActionExecuter {

	static Logger logger = Logger.getLogger(ActionExecuter.class.getName());
	static final boolean logFine = Configuration.getInstance().isLogFine();	
	private ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
	private final String localHostName = Configuration.getInstance().getLocalHostName();
	
	private AllSessionPools allSessionPools = AllSessionPools.getInstance();
	private ActionDAO actionDAO = new ActionDAO();

	
	/**
	 * As soon as this method returns the client will start a thread to display chunks of output from the session.
	 * There is no need to wait for the script to finish before returning.
	 * 
	 * There are two processes here. The chunked output from the session is the ssh process running commands on
	 * the remote server. The other process is the script sending web requests containing commands that are run by the ssh.
	 */
	public CommandResult executeAction(String environmentName, EnvironmentViewRow environmentViewRow) {
		CommandResult commandResult;
		Action action = actionDAO.loadAction(environmentName, environmentViewRow);
		String commandLine = action.getCommandLine();
		Session outputCollectingSession = null;
		String outputCollectingServer;
		// Always need a separate outputCollectingSession for scripts to keep the local script stdout separate from remote output.
		if (commandLine.lastIndexOf("OnAllServers") > 0 || environmentViewRow.getServerName().startsWith("all servers") || commandLine.startsWith("scripts/")) {
			outputCollectingServer = localHostName;
		} else {
			outputCollectingServer = environmentViewRow.getServerName();
		}
		outputCollectingSession = makeOutputCollectingSession(environmentName, outputCollectingServer, action, environmentViewRow);
		singleThreadExecutor.execute(new ActionOutputRunnable(environmentName, environmentViewRow, outputCollectingSession, action));
		commandResult = new CommandResult();
		commandResult.setSessionId(outputCollectingSession.getSessionId());
		commandResult.setCommandStatus(CommandStatus.RUNNING);
		outputCollectingSession.setLastCommandStatus(CommandStatus.RUNNING);
		return commandResult;
	}
	
	private Session makeOutputCollectingSession(String environmentName, String serverName, Action action, EnvironmentViewRow environmentViewRow) {
		Session session = null;
		Server server = new Server(serverName);
		ServerSessionPool ssp = allSessionPools.getServerSessionPool(server);
		session = ssp.getSession(String.format("ActionExecuter for %s on %s", action.getOutputName(), serverName));
		session.updateLastUsed(); // to protect it from FinishedActionSessionCloser
		session.setSessionType(SessionType.ACTION);
		session.setAction(action);
		session.setEnvironmentName(environmentName);
		session.setKillSubprocesesWhenFinished(false);
		String commandLine = action.getCommandLine();
		if (commandLine.lastIndexOf("OnAllServers") > 0 || 
			environmentViewRow.getServerName().startsWith("all servers") ||
			commandLine.startsWith("scripts/")) {
			if (commandLine.lastIndexOf("Heartbeat") == -1) {
				session.getChunkedOutput().setNonStandardArraySize(10000);	
			}
		}
		return session;
	}

	public void setActionDAO(ActionDAO actionDAO) {
		this.actionDAO = actionDAO;
	}

}
