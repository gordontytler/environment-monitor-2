package monitor.implementation.action;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import monitor.implementation.session.AllSessionPools;
import monitor.implementation.session.ChildProcessFinder;
import monitor.implementation.session.ProcessKiller;
import monitor.implementation.session.Session;
import monitor.model.Command;
import monitor.model.CommandResult;
import monitor.model.Configuration;

/**
 * Protection against clogging up servers by running the same output multiple times.  
 * This could happen if there are several instances of environment-monitor running.
 * Outputs that are no longer being used should be killed automatically by FinishedActionSessionCloser.
 * The EnvironmentViewBuilder should not attempt to start an output if it is already running on
 * the server. Furthermore, the restartOutputs should end all running actions and the shutdown hook should also tidy up.
 * <p>
 * But, should any of the above not work this class checks for output already running
 * before starting a new one.
 * <p>
 * A cache is checked for the command line last time it was run. If found in the cache it checks
 * for a process with this command line.
 * <p>
 * If the command is allowed to run ActionOutputRunnable will call newOutputStarted. This gets the command line for
 * the process and puts it in the cache.
 * <p>
 * After restart outputs the definitions in data/applications may have changed. This won't be a problem because we always
 * replace the cache entry when newOutputStarted. The session is given a reference to the AlreadyRunningInfo which can be used   
 * by ProcessKiller to check that the process it is about to kill is running the expected command.
 */

public class AlreadyRunningChecker {
	
	static Logger logger = Logger.getLogger(AlreadyRunningChecker.class.getName());
	static final boolean logFine = Configuration.getInstance().isLogFine();
	
	private final boolean ACTION_ALREADY_RUNNING_KILL = Configuration.getInstance().isActionAlreadyRunningKill();
	private final boolean ACTION_ALREADY_RUNNING_RUN_ANOTHER = Configuration.getInstance().isActionAlreadyRunningRunAnother();
	
	private AllSessionPools allSessionPools = AllSessionPools.getInstance();
	private ProcessKiller processKiller = new ProcessKiller();
	private ChildProcessFinder childProcessFinder = new ChildProcessFinder();

	private ConcurrentHashMap<AlreadyRunningKey, AlreadyRunningInfo> alreadyRunningCache = new ConcurrentHashMap<AlreadyRunningKey, AlreadyRunningInfo>(200);
	
	private static AlreadyRunningChecker theInstance = new AlreadyRunningChecker();
	
	public static AlreadyRunningChecker getInstance() {
		return theInstance;
	}
	
	public AlreadyRunningCheckerResult shouldRun(Session session, String nameInEnvironmentView, String outputName) {
		AlreadyRunningCheckerResult shouldRun = new AlreadyRunningCheckerResult(true);
		if (ACTION_ALREADY_RUNNING_RUN_ANOTHER == true && ACTION_ALREADY_RUNNING_KILL == false) {
			return shouldRun;
		}
		AlreadyRunningKey key = new AlreadyRunningKey(session.getServer().getHost(), nameInEnvironmentView, outputName);
		AlreadyRunningInfo info = alreadyRunningCache.get(key);
		if (info == null) {
			if (logFine) logger.info(String.format("key not found in alreadyRunningCache - %s", key));
			return shouldRun;
		}
		// we know the command last time it was run so check if any processes have this command
		Session controlSession = allSessionPools.getServerSessionPool(session.getServer()).getControlSession("->AlreadyRunningChecker.shouldRun");
		shouldRun.setControlSession(controlSession);
		// TODO problem some commands will have characters that will be treated as special regex characters by the grep 
		Command command = new Command("ps -e -o pid,command= | grep '" + info.getCommand() + "' | grep -v grep | awk '{print($1)}'", false);
		CommandResult commandResult = controlSession.executeCommand(command);
		ArrayList<Integer> pids;
		try {
			pids = childProcessFinder.processOutputToList(commandResult.getOutput());
		} catch (Exception e) {
			logger.log(Level.SEVERE, String.format("Command %s returned %s which caused: ", command.getRequest(), commandResult.getOutput()), e);
			return shouldRun;
		}
		if (pids.size() > 0) {
			if (ACTION_ALREADY_RUNNING_KILL) {
				for (int pid : pids) {
					processKiller.killProcessAndSubProcesses(session.getServer(), pid, controlSession, "->AlreadyRunningChecker.shouldRun");
				}
			}
			shouldRun.setShouldRun(ACTION_ALREADY_RUNNING_RUN_ANOTHER);
			if (!ACTION_ALREADY_RUNNING_RUN_ANOTHER) {
				logger.info(String.format("Action is already running. Won't run another. Key: %s command: %s", key, info));
			}
		}
		return shouldRun;
	}

	public void newOutputStarted(AlreadyRunningCheckerResult shouldRun, Session session, String nameInEnvironmentView, String outputName) {
		if (ACTION_ALREADY_RUNNING_RUN_ANOTHER == true && ACTION_ALREADY_RUNNING_KILL == false) {
			return;
		}
		Session controlSession = shouldRun.getControlSession();
		if (controlSession == null) {
			controlSession = allSessionPools.getServerSessionPool(session.getServer()).getControlSession("->AlreadyRunningChecker.newOutputStarted");
		}
		
		String runningCommand = null;
		int attempts = 0;
		// This loop is a work around for a bug.  The 'ps -p %d -o command=' sometimes picks up the last output from getChildren
		// On the next loop getChildren fails with:  java.lang.NumberFormatException: For input string: "xargs tail -F"
		// It seems each command was getting the output from the previous command.
		while (attempts < 3 && runningCommand == null) {
			ArrayList<Integer> pids;
			try {
				pids = processKiller.getChildren(session.getBashProcessId(), controlSession);
			} catch (NumberFormatException nfe) { // The second attempt fails here
				logger.log(Level.SEVERE, String.format("sessionId:%s output to getChildren was not a number. Creating a new session.", controlSession.getSessionId()), nfe);
				controlSession.setKillSubprocesesWhenFinished(false); // TODO - there is another bug, this should already be set
				controlSession.tidyUpAfterException("->AlreadyRunningChecker.newOutputStarted");
				controlSession = allSessionPools.getServerSessionPool(session.getServer()).getControlSession("->AlreadyRunningChecker.newOutputStarted->catch");
				attempts++;
				continue;
			}
			if (pids.size() > 0) {
				Command command = new Command(String.format("ps -p %d -o command=", pids.get(0)), false);
				CommandResult commandResult = controlSession.executeCommand(command);
				String possibleRunningCommand = commandResult.getOutput().trim();
				if (possibleRunningCommand.length() == 0) {
					logger.info(String.format("Did not find process %d will assume it has finished doing: %s", session.getBashProcessId(), session.getRunningCommand()));
					break;					
				} else {
					try {
						Integer.parseInt(possibleRunningCommand);
						attempts++; // The first attempt fails here
						logger.log(Level.SEVERE, String.format("Failed attempt %d to find command line using '%s'. Got stale output: %s'", attempts, command.getRequest(), possibleRunningCommand));
					} catch (Exception e) {
						// it should not be a number so this is the good path
						runningCommand = possibleRunningCommand;
						AlreadyRunningKey key = new AlreadyRunningKey(session.getServer().getHost(), nameInEnvironmentView, outputName);
						AlreadyRunningInfo info = new AlreadyRunningInfo(runningCommand, System.currentTimeMillis(), session.getSessionId());
						alreadyRunningCache.put(key, info);
						session.setAlreadyRunningInfo(info);
					}
				}
			} else {
				logger.info(String.format("Got no child processes for %d will assume it has finished or not started doing: %s", session.getBashProcessId(), session.getRunningCommand()));
				break;
			}
		}
		if (controlSession != null) {
			controlSession.close("AlreadyRunningCheckerResult.newOutputStarted");
		}
	}
	
	public String getAlreadyRunningCache() {
		StringBuilder sb = new StringBuilder(10000);
		for (Entry<AlreadyRunningKey, AlreadyRunningInfo> entry : alreadyRunningCache.entrySet()) {
			sb.append(entry.getKey().toString()).append("\t").append(entry.getValue().toString()).append("\n");
		}
		if (sb.length() == 0) {
			sb.append("The already running cache is empty.");
		}
		return sb.toString();
	}
	
}
