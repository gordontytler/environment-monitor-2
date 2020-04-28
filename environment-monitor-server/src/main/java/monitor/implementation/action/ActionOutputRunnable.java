package monitor.implementation.action;

import monitor.dao.EnvironmentViewDAO;
import monitor.implementation.MonitorRuntimeException;
import monitor.implementation.environment.EnvironmentViewBuilder;
import monitor.implementation.session.AllSessionPools;
import monitor.implementation.session.ServerSessionPool;
import monitor.implementation.session.Session;
import monitor.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * To make the client responsive, executeAction returns with status CommandStatus.RUNNING while this thread continues
 * with the processing. It calls appendToChunkedOutput. The client OutputChunkWorker will see this output.
 * <p>
 * Commands for all servers in an environment are defined in data/environment/defaults.txt or in the specific file for the
 * environment. There are two ways to run a command on all servers in an environment
 *  <ol>
 *  <li>find-rule-all = scripts/commandOnAllServers001.py "ls /var/app/*rule* -al"
 *  <li>umask-all = ls /var/app/*rule* -al
 *  <ol>
 * The python program can be copied and extended e.g. copyBashScriptToAllServersAndRunIt.py
 **/
public class ActionOutputRunnable implements Runnable {

    static final boolean logFine = Configuration.getInstance().isLogFine();
    static Logger logger = Logger.getLogger(ActionExecuter.class.getName());
    private final String monitorHostAndPort = Configuration.getInstance().getMonitorHostAndPort();
    private final String fullPathToData = Configuration.getInstance().getFullPathToInstallDirectory() + "/" + Configuration.getInstance().getDataDirectory();

    private final AllSessionPools allSessionPools = AllSessionPools.getInstance();
    private final EnvironmentViewDAO environmentViewDAO = EnvironmentViewDAO.getInstance();
    private final EnvironmentViewBuilder environmentViewBuilder;
    private final AlreadyRunningChecker alreadyRunningChecker = AlreadyRunningChecker.getInstance();
    private final String environmentName;
    private final EnvironmentViewRow environmentViewRow;
    private final Session outputCollectingSession;
    private final Action action;

    public ActionOutputRunnable(String environmentName, EnvironmentViewRow environmentViewRow, Session outputCollectingSession, Action action) {
        this.environmentName = environmentName;
        this.environmentViewRow = environmentViewRow;
        this.outputCollectingSession = outputCollectingSession;
        this.action = action;
        this.environmentViewBuilder = EnvironmentViewBuilder.getInstance(environmentName);
    }


    @Override
    public void run() {
        Thread.currentThread().setName(environmentName + " - " + environmentViewRow.getOutputName() + " on " + environmentViewRow.getServerName());

        List<Session> sessions = new ArrayList<Session>();
        Command command;
        CommandResult commandResult;

        outputCollectingSession.appendToChunkedOutput(""); // prevents the first chunk from being lost - don't know why
        String unsubstitutedCommandLine = action.getCommandLine();
        String commandLine = environmentViewDAO.substituteVariables(unsubstitutedCommandLine, environmentViewRow.getApplicationName());

        // TODO on "all servers" and "scripts/" and row.getOutputName().startsWith("Execute...") should be attributes in the object model
        if (commandLine.lastIndexOf("OnAllServers") > 0 || environmentViewRow.getServerName().startsWith("all servers")) {
            if (environmentViewBuilder.isEnvironmentBeingRestarted()) {
                String message = String.format("environment '%s' is being restarted. Try again later. Command: '%s'", environmentName, commandLine);
                outputCollectingSession.appendToChunkedOutput(message);
                logger.info(message);
                return;
            }
            List<String> serverNames = environmentViewDAO.getServerNames(environmentName);
            makeSessionsOnServers(serverNames, environmentName, action, sessions);
        } else {
            // one server but a script so needs a separate outputCollectingSession otherwise local Python print to stdout is mixed up with remote server http get executeCommand output
            if (commandLine.startsWith("scripts/")) {
                List<String> serverNames = new ArrayList<String>(1);
                serverNames.add(environmentViewRow.getServerName());
                makeSessionsOnServers(serverNames, environmentName, action, sessions);
            } else {
                // running one command directly on one server so only need outputCollectingSession
                sessions.add(outputCollectingSession);
            }
        }
        if (sessions.size() == 0) {
            outputCollectingSession.appendToChunkedOutput("\nGot no server sessions.");
            throw new MonitorRuntimeException("Got no server sessions.");
        }
        // run the script
        if (commandLine.startsWith("scripts/")) {
            command = makeCommand(environmentName, commandLine, sessions);
            outputCollectingSession.appendToChunkedOutput(String.format("\n\nRunning script> %s\n", command.getRequest()));
            command.setMillisBeforeTimeout(1000);
            executeCommand(command, outputCollectingSession);
            outputCollectingSession.updateLastUsed(); // want to keep the output so will let FinishedActionSessionCloser close the session
        } else {
            // or run some command directly on the remote machine such as tail -
            if (sessions.size() == 1) {
                command = new Command(commandLine);
                Session session = sessions.get(0);
                AlreadyRunningCheckerResult shouldRun = alreadyRunningChecker.shouldRun(session, environmentViewRow.getApplicationName(), environmentViewRow.getOutputName());
                if (shouldRun.isShouldRun()) {
                    session.appendToChunkedOutput(String.format("\nRunning command> %s\n", commandLine));
                    commandResult = executeCommand(command, session);
                    session.appendToChunkedOutput(String.format("\nCommand status: %s\n", commandResult.getCommandStatus()));
                    alreadyRunningChecker.newOutputStarted(shouldRun, session, environmentViewRow.getApplicationName(), environmentViewRow.getOutputName());
                } else {
                    session.appendToChunkedOutput(String.format("\nWon't start output because it is already running on %s. Command: '%s'\n", session.getServer().getHost(), commandLine));
                }
            } else {
                outputCollectingSession.appendToChunkedOutput(String.format("\n\nRunning command> %s\n", commandLine));
                for (Session session : sessions) {
                    command = new Command(commandLine);
                    outputCollectingSession.appendToChunkedOutput(String.format("\n%s$ ", session.getServer().getHost()));
                    AlreadyRunningCheckerResult shouldRun = alreadyRunningChecker.shouldRun(session, environmentViewRow.getApplicationName(), environmentViewRow.getOutputName());
                    if (shouldRun.isShouldRun()) {
                        commandResult = executeCommand(command, session);
                        outputCollectingSession.appendToChunkedOutput(commandResult.getOutput().trim());
                        alreadyRunningChecker.newOutputStarted(shouldRun, session, environmentViewRow.getApplicationName(), environmentViewRow.getOutputName());
                    } else {
                        outputCollectingSession.appendToChunkedOutput(String.format("command is already running: '%s'", commandLine));
                    }
                }
                outputCollectingSession.appendToChunkedOutput(String.format("\n\nFinished."));
            }
        }
    }

    private CommandResult executeCommand(Command command, Session session) {
        CommandResult commandResult = null;
        try {
            commandResult = session.executeCommand(command);
        } catch (Exception e) {
            commandResult = new CommandResult();
            commandResult.setCommandStatus(CommandStatus.ERROR);
            outputCollectingSession.appendToChunkedOutput(String.format("%s sessionId:%s - %s", session.getServer(), session.getSessionId(), e.getMessage()));
        }
        return commandResult;
    }


    private void makeSessionsOnServers(List<String> serverNames, String environmentName, Action action, List<Session> sessions) {
        if (serverNames.size() > 1) {
            outputCollectingSession.appendToChunkedOutput("\n_______________________________________________________________________________________________________________________");
            outputCollectingSession.appendToChunkedOutput("\n" + "Getting sessions on: " + serverNames);
        }
        for (String serverName : serverNames) {
            try {
                makeSession(environmentName, serverName, action, sessions);
            } catch (Exception e) {
                StringBuilder message = new StringBuilder();
                message.append("\n").append(e.getMessage());
                Throwable cause = e.getCause();
                Throwable lastCause = null;
                while (cause != null) {
                    lastCause = cause;
                    cause = cause.getCause();
                }
                if (lastCause != null) {
                    message.append(String.format("\nCaused by: %s\nThis happened with server: %s", lastCause.toString(), serverName));
                }
                outputCollectingSession.appendToChunkedOutput(message.toString());
                logger.log(Level.SEVERE, message.toString());
            }
        }
        if (serverNames.size() > 1) {
            outputCollectingSession.appendToChunkedOutput("\n_______________________________________________________________________________________________________________________");
        }

        if (logFine) logger.info("serverNames: " + serverNames);
    }


    private Session makeSession(String environmentName, String serverName, Action action, List<Session> sessions) {
        Session session = null;
        Server server = new Server(serverName);
        ServerSessionPool ssp = allSessionPools.getServerSessionPool(server);
        if (ssp.cantLogonReason().length() > 0) {
            outputCollectingSession.appendToChunkedOutput("\n" + ssp.cantLogonReason());
            logger.log(Level.SEVERE, ssp.cantLogonReason());
        } else {
            session = ssp.getSession(String.format("ActionOutputRunnable for %s on %s", action.getOutputName(), serverName));
            outputCollectingSession.appendToChunkedOutput("\nGot session. " + session.toStatusString().trim());
            session.updateLastUsed(); // to protect it from FinishedActionSessionCloser
            session.setSessionType(SessionType.ACTION);
            session.setAction(action);
            session.setEnvironmentName(environmentName);
            session.setKillSubprocesesWhenFinished(false);
            sessions.add(session);
        }
        return session;
    }


    /**
     * Constructs commands lines like these:<br><br>
     * python full-path-to-data/scripts/discoverApps001.py 127.0.0.1:8084 14,15,16 heartbeat<br>
     * python full-path-to-data/scripts/commandOnAllServers001.py 127.0.0.1:8084 14,15,16 "sudo sed 's/umask 077/umask 022/g' /etc/profile"
     *
     * @param environmentName
     * @param commandLine     the script file and it's arguments. The monitorHostAndPort and sessions are inserted before the arguments.
     * @param sessions
     * @return
     */
    private Command makeCommand(String environmentName, String commandLine, List<Session> sessions) {
        String program;
        int lastSpace = commandLine.indexOf(' ');
        String uptoFirstSpace = lastSpace == -1 ? commandLine : commandLine.substring(0, commandLine.indexOf(' '));
        if (uptoFirstSpace.endsWith(".py")) {
            program = "python -u";
        } else if (uptoFirstSpace.endsWith(".pl")) {
            program = "perl";
        } else if (uptoFirstSpace.endsWith(".sh")) {
            program = "sh";
        } else {
            throw new MonitorRuntimeException("Script file does not end in .py .pl or .sh Can't determine which program to run :" + commandLine);
        }
        String arguments = "";
        if (lastSpace > -1) {
            arguments = commandLine.substring(lastSpace + 1);
        }

        StringBuilder line = new StringBuilder(String.format("%s %s/%s %s %s", program, fullPathToData, uptoFirstSpace, monitorHostAndPort, sessions.get(0).getSessionId()));
        for (int i = 1; i < sessions.size(); i++) {
            line.append(",").append(sessions.get(i).getSessionId());
        }
        if (arguments.length() > 0) {
            line.append(" ").append(arguments);
        }
        if (logFine) logger.info(line.toString());
        return new Command(line.toString());
    }

}
