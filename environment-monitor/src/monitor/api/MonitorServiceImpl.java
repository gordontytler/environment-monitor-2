package monitor.api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import monitor.dao.ApplicationCache;
import monitor.dao.EnvironmentNamesDAO;
import monitor.dao.EnvironmentViewDAO;
import monitor.implementation.MonitorRuntimeException;
import monitor.implementation.action.ActionExecuter;
import monitor.implementation.environment.EnvironmentViewBuilder;
import monitor.implementation.session.AllSessionPools;
import monitor.implementation.session.Session;
import monitor.implementation.shell.ChunkedOutput;
import monitor.model.Application;
import monitor.model.Command;
import monitor.model.CommandResult;
import monitor.model.CommandStatus;
import monitor.model.Configuration;
import monitor.model.EnvironmentView;
import monitor.model.EnvironmentViewRow;
import monitor.model.LogonResult;
import monitor.model.OutputChunkResult;
import monitor.model.OutputHistory;
import monitor.model.Server;

@WebService(
	       name="MonitorService",
	       serviceName="MonitorService",
	       targetNamespace="http://MonitorService"
	   )

/*

To generate wsdl run this in the directory containing src and bin 
 
wsgen -cp ./bin/ -keep -d ./bin/ -s ./src-generated/ -r ./src-generated/ -wsdl monitor.api.MonitorServiceImpl

Then, cd ../environment-monitor-client/  and run 

wsimport -keep -d ./bin/ -s ./src-generated/ ../environment-monitor/src-generated/MonitorService.wsdl

 */
public class MonitorServiceImpl {

	static Logger logger = Logger.getLogger(MonitorServiceImpl.class.getName());
	private static final boolean logFine = Configuration.getInstance().isLogFine();	
	
	private AllSessionPools allSessionPools = AllSessionPools.getInstance();	
	private ActionExecuter actionExecuter = new ActionExecuter();
	private EnvironmentNamesDAO environmentNamesDAO = EnvironmentNamesDAO.getInstance();
	private EnvironmentViewDAO environmentViewDAO = EnvironmentViewDAO.getInstance();
	private ApplicationCache applicationCache = ApplicationCache.getInstance();
	private static final int ALL_OUTPUT = 0;

	@WebResult(name = "loginResult")
	public LogonResult logon(@WebParam(name="host") String host, @WebParam(name="hostName") String hostName, @WebParam(name="environmentName") String environmentName) {
		LogonResult logonResult = new LogonResult();
		Server server = new Server(host);
		Session session;
		try {
			session = allSessionPools.getServerSessionPool(server).getSession("MonotorServiceImpl.logon");
			session.makeLoggedOnMessage();
			// we set the environmentName so that we can test automation scripts using a session created by Server->Open Terminal
			if (environmentName != null) {
				environmentViewDAO.loadUnsafeEnvironmentView(environmentName);
				session.setEnvironmentName(environmentName);
			}
			logonResult.setSessionId(session.getSessionId());
			logonResult.setBashProcessId(session.getBashProcessId());
			logonResult.setCommandStatus(CommandStatus.FINISHED);
		} catch (Exception e) {
			logonResult.setErrorMessage(StackTraceFormatter.asString(e));
			logonResult.setCommandStatus(CommandStatus.ERROR);
		}
		return logonResult;
	}

	/** Close session but leave ssh connection open for reuse. */
	public void close(@WebParam(name="sessionId") String sessionId)  {
		Session	session = allSessionPools.getSessionUsingSessionId(sessionId);
		logger.info(String.format("closing %s", session));		
		if (session != null) {
			session.close("MonitorServiceImpl.close");
		}
	}
	
	public OutputChunkResult getOutputChunk(@WebParam(name="sessionId") String sessionId, @WebParam(name="chunkNumber") int chunkNumber) {
		Session session = allSessionPools.getSessionUsingSessionId(sessionId);
		if (session == null) {
			throw new MonitorRuntimeException(String.format("sessionId %s not found.\n", sessionId));
		}
		if (logFine) logger.info(String.format("getOutputChunk sessionId %s chunkNumber %d", sessionId, chunkNumber));
		ChunkedOutput chunkedOutput = session.getChunkedOutput();
		OutputChunkResult result = new OutputChunkResult();
		if (chunkNumber == ALL_OUTPUT) {
			result.setOutput(chunkedOutput.getAllChunks());
			result.setChunkNumber(chunkedOutput.getHighestChunkNumber());
		} else {
			result.setOutput(chunkedOutput.getChunk(chunkNumber));
			result.setChunkNumber(chunkNumber);			
		}
		result.setLowestChunk(chunkedOutput.getLowestChunkNumber());
		result.setHighestChunk(chunkedOutput.getHighestChunkNumber());
		result.setSessionId(sessionId);
		result.setCommandStatus(session.getLastCommandStatus());
		if (logFine) logger.info(String.format("getOutputChunk sessionId %s lowest %d highest %d %s", sessionId, result.getLowestChunk(), result.getHighestChunk(), result.getCommandStatus() == null ? "null" :result.getCommandStatus().toString() ));
		return result;
	}
	
	public CommandResult executeCommand(@WebParam(name="command") String command, @WebParam(name="sessionId") String sessionId)  {
		CommandResult commandResult = null;
		Session session = null;
		Command cmd = new Command(command);
		try {
			session = allSessionPools.getSessionUsingSessionId(sessionId);
			if (session == null) {
				commandResult = formatNoSessionException(sessionId);
			} else {
				commandResult = session.executeCommand(cmd);
			}
		} catch (Exception e) {
			commandResult = formatExceptionAndCreateNewSession("\n\nException caught processing command '" + command + "'", e, session);			
		}
		return commandResult;
	}
	
	
	public EnvironmentView getEnvironmentView(@WebParam(name="environmentName") String environmentName, @WebParam(name="outputHistoryTimeStamp") long outputHistoryTimeStamp)  {
		EnvironmentView environmentView = new EnvironmentView();
		try {
			environmentView = EnvironmentViewBuilder.getInstance(environmentName).getEnvironmentView(outputHistoryTimeStamp);
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e); 
			// This is to get the stack trace for the very rare ConcurrentModificationException and allow the client to continue.
		}
		if (logger.isLoggable(Level.FINE)) {
			List<EnvironmentViewRow> rows = environmentView.getRows();
			List<OutputHistory> histories = null;
			if (rows.size() > 0) {
				histories = rows.get(rows.size() - 1).getOutputHistory();
			}
			logger.info(String.format("%s TimeStamp %d, histories %d", environmentName, outputHistoryTimeStamp, histories == null ? 0 : histories.size()));
		}
		return  environmentView;
	}
	
	/** An action is one or more pre-defined commands that may produce continuous output. 
	 * They are started automatically by {@link EnvironmentViewBuilder#addPreDefinedActionRow} 
	 * or can be selected from the menu. Actions are defined in the data/environment files and 
	 * data/application files. */
	public CommandResult executeAction(@WebParam(name="environmentName") String environmentName, @WebParam(name="environmentViewRow") EnvironmentViewRow environmentViewRow) {
		logger.info(String.format("\n\nexecuting action '%s' on %s", environmentViewRow.getOutputName(), environmentViewRow.getServerName()));
		CommandResult commandResult = null;
		try {
			commandResult = actionExecuter.executeAction(environmentName, environmentViewRow);
		} catch (Exception e) {
			commandResult = formatException(e);
		}
		logger.info(String.format("Finished starting the execution of action '%s' on %s\n\n", environmentViewRow.getOutputName(), environmentViewRow.getServerName()));		
		return commandResult;
	}
	
	/** Try to kill the current command or create a new session. */
	public CommandResult killRunningCommand(@WebParam(name="sessionId") String sessionId)  {
		CommandResult commandResult = null;
		Session session = null;
		try {
			session = allSessionPools.getSessionUsingSessionId(sessionId);
			if (session == null) {
				commandResult = formatNoSessionException(sessionId);
			} else {
				commandResult = session.killRunningCommand("MonitorServiceImpl.killrunningCommand");
			}
		} catch (Exception e) {
			commandResult = formatExceptionAndCreateNewSession("\n\nException caught attempting to kill command.", e, session);
		}
		return commandResult;
	}

	@WebMethod
	@WebResult(name = "environmentNames")
	public List<String> getEnvironmentNames() {
		return environmentNamesDAO.getEnvironmentNames();
	}


	@WebMethod
	@WebResult(name = "addServerResult")
	public CommandResult addServer(@WebParam(name="environmentName") String environmentName, @WebParam(name="serverName") String serverName) {
		CommandResult commandResult = null;
		try {
			commandResult = environmentViewDAO.addServer(environmentName, serverName);
		} catch (Exception e) {
			commandResult = formatException(e);
		}
		return commandResult;
	}	
	
	@WebMethod
	@WebResult(name = "deleteRowResult")
	public CommandResult deleteRow(@WebParam(name="environmentName") String environmentName, @WebParam(name="index") int index) {
		CommandResult commandResult = null;
		try {
			commandResult = environmentViewDAO.deleteRow(environmentName, index);
		} catch (Exception e) {
			commandResult = formatException(e);
		}
		return commandResult;
	}	
		
	@WebMethod
	@WebResult(name = "deleteEnvironmentResult")
	public CommandResult deleteEnvironment(@WebParam(name="environmentName") String environmentName) {
		CommandResult commandResult = new CommandResult(CommandStatus.FINISHED, "", 0, null);
		try {
			environmentViewDAO.deleteEnvironmentView(environmentName);
		} catch (Exception e) {
			commandResult = formatException(e);
		}
		return commandResult;
	}	

	@WebMethod
	@WebResult(name = "saveEnvironmentResult")
	public CommandResult saveEnvironment(@WebParam(name="environmentName") String environmentName) {
		CommandResult commandResult = new CommandResult(CommandStatus.FINISHED, "", 0, null);
		try {
			environmentViewDAO.saveEnvironmentView(environmentName);
		} catch (Exception e) {
			commandResult = formatException(e);
		}
		return commandResult;
	}	

	@WebMethod
	@WebResult(name = "renameEnvironmentResult")
	public CommandResult renameEnvironment(@WebParam(name="oldName") String oldName, @WebParam(name="newName") String newName) {
		CommandResult commandResult = new CommandResult(CommandStatus.FINISHED, "", 0, null);
		try {
			environmentViewDAO.renameEnvironmentView(oldName, newName);
		} catch (Exception e) {
			commandResult = formatException(e);
		}
		return commandResult;
	}	
	
	
	@WebMethod
	@WebResult(name = "restartOutputsResult")
	public CommandResult restartOutputs(@WebParam(name="environmentName") String environmentName) {
		CommandResult commandResult = new CommandResult(CommandStatus.FINISHED, "", 0, null);
		try {
			EnvironmentViewBuilder.getInstance(environmentName).restartOutputs();
		} catch (Exception e) {
			commandResult = formatException(e);
		}
		return commandResult;
	}	
	
	
	
	@WebMethod
	@WebResult(name = "addApplicationResult")
	public CommandResult addApplication(
			@WebParam(name="sessionId") String sessionId, 
			@WebParam(name="nameInEnvironmentView") String nameInEnvironmentView, 
			@WebParam(name="fileName") String fileName,
			@WebParam(name="outputName") String outputName) {
		CommandResult commandResult = null;
		Session session = null;
		try {
			session = allSessionPools.getSessionUsingSessionId(sessionId);
			if (session == null) {
				commandResult = formatNoSessionException(sessionId);
			} else {
				commandResult = environmentViewDAO.addApplication(session.getEnvironmentName(), session.getServer().getHost(), nameInEnvironmentView, fileName, outputName);
			}
		} catch (Exception e) {
			commandResult = formatException(e);
		}
		return commandResult;
	}

	
	@WebMethod
	@WebResult(name = "application")
	public Application loadApplicationByFileName(@WebParam(name="fileName") String fileName, @WebParam(name="nameInEnvironmentView") String nameInEnvironmentView) {
		return applicationCache.loadApplicationByFileName(fileName, nameInEnvironmentView);
	}
	
	
	private CommandResult formatNoSessionException(String sessionId) {
		CommandResult commandResult = new CommandResult();
		commandResult.setCommandStatus(CommandStatus.ERROR);
		commandResult.setOutput(String.format("sessionId %s not found.\n", sessionId));
		return commandResult;
	}
	
	private CommandResult formatException(Exception e) {
		CommandResult commandResult = new CommandResult();
		commandResult.setCommandStatus(CommandStatus.ERROR);
		commandResult.setOutput(StackTraceFormatter.asString(e));
		return commandResult;
	}
	
	private CommandResult formatExceptionAndCreateNewSession(String message, Exception e, Session session) {
		CommandResult commandResult = new CommandResult();
		commandResult.setCommandStatus(CommandStatus.ERROR);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		StackTraceFormatter.getStackTrace(e, out);
		try {
			out.write(message.getBytes());
			out.write((" Creating new SSH session.\n").getBytes());
			session.tidyUpAfterException("MonitorServiceImpl.formatExceptionAndCreateNewSession");
			session = allSessionPools.getServerSessionPool(session.getServer()).getSession("MonitorServiceImpl.formatExceptionAndCreateNewSession");
			commandResult.setSessionId(session.getSessionId());
		} catch (IOException e2) {
			logger.log(Level.SEVERE, "Problem attempting to recover from error.", e2);
		}
		commandResult.setOutput(out.toString());
		return commandResult;
	}
	
	
}
