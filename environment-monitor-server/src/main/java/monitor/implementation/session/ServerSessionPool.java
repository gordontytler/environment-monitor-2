package monitor.implementation.session;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import monitor.implementation.MonitorNoStackTraceRuntimeException;
import monitor.implementation.MonitorRuntimeException;
import monitor.model.Command;
import monitor.model.CommandResult;
import monitor.model.CommandStatus;
import monitor.model.Configuration;
import monitor.model.Server;
import monitor.model.SessionType;

/**
 * I allow ssh sessions to be reused. 
 */
public class ServerSessionPool {

	static Logger logger = Logger.getLogger(ServerSessionPool.class.getName());

	private static int MAX_SESSIONS = Configuration.getInstance().getMaximumServerSessions();
	private static final int MAX_TERMINALS_PER_SESSION = Configuration.getInstance().getMaximumTerminalsPerSSHConnection();
	private static final int MINUTES_TO_WAIT_AFTER_FAILED_LOGIN =  Configuration.getInstance().getMinutesToWaitAfterFailedLogin();
	private static final int DEFAULT_TEST_COMMAND_TIMEOUT_MILLIS = Configuration.getInstance().getTestCommandTimeoutMillis();
	private static final String MAX_SESSIONS_ERROR = "Maximum of %d sessions are open on server %s.\n    Will not create a new one.";
	
	// The sessions for one server. Each session uses the Stdin and Stdout for one terminal.
	protected List<Session> sessions = new ArrayList<Session>(MAX_SESSIONS);
	/** only 10 terminals can share the same ssh connection to the server see {@link TooManySessionsTest}  */
	private List<SSHConnection> sshConnections = new ArrayList<SSHConnection>();	

	
	private AllSessionPools allSessionPools = AllSessionPools.getInstance();
	private final Server server;
	private String tooManySessions;
	private long timeOfLastError = 0;

	private volatile boolean inTheMiddleOfCreatingASessionNow;
	private String previousFailedLoginReason = "unknown";
	private boolean alreadyTriedToCreateAutoUser = false;


	ServerSessionPool(Server server) {
		this.server = server;
		tooManySessions = String.format(MAX_SESSIONS_ERROR, MAX_SESSIONS, this.server);
	}

	public String cantLogonReason() {
		if (numberOfOpenSessions() >= MAX_SESSIONS) {
			return tooManySessions;
		}
		if (timeOfLastError > 0 && !inTheMiddleOfCreatingASessionNow) {
			long mins = (System.currentTimeMillis() - timeOfLastError) / (1000 * 60);
			if (mins < MINUTES_TO_WAIT_AFTER_FAILED_LOGIN) {
				return String.format("Won't attempt logon to %s for another %d minutes. Previous failed logon was %d minutes ago.\n" +
						"    Previous failure: %s", this.server, MINUTES_TO_WAIT_AFTER_FAILED_LOGIN - mins, mins, previousFailedLoginReason);
			}
		}
		if (allSessionPools.isShutdownThreadIsRunning()) {
			return "Shutdown thread is running.";
		}
		return "";
	}
	
	/*
	 * Returns an unused session from the pool for the server or creates a new one.
	 * This method is synchronized on the ServerSessionPool object for one server so it
	 * is OK to make two threads asking for a session on the same server to block 
	 */
	public synchronized Session getSession(String calledBy) {
		Session session = findClosedSessionAndOpenIt(true);
		if (session != null) {
			return session;
		}
		if (numberOfOpenSessions() < MAX_SESSIONS) {
			timeOfLastError = System.currentTimeMillis();
			inTheMiddleOfCreatingASessionNow = true;
			try {
				session = createSessionAndLogon(calledBy, DEFAULT_TEST_COMMAND_TIMEOUT_MILLIS);
			} catch (Exception e) {
				StringBuilder causedBy = new StringBuilder();
				Throwable cause = e.getCause();
				Throwable lastCause = null;
				while (cause != null) {
					lastCause = cause;
					cause = cause.getCause();
				}
				if (lastCause != null) {
					causedBy.append("\n    Caused by: ").append(lastCause.toString());
				}
				previousFailedLoginReason = e.toString() + causedBy;
				throw new MonitorRuntimeException(e);
			} finally {
				inTheMiddleOfCreatingASessionNow = false;
			}
			timeOfLastError = 0;
			return session;
		}
		throw new MonitorNoStackTraceRuntimeException(tooManySessions);
	}


	private Session createSessionAndLogon(String calledBy, int testCommandTimeoutMillis) {
		PseudoTerminal terminal = createPseudoTerminal();
		Session session = new Session(server, terminal, calledBy, testCommandTimeoutMillis);
		// three passwords are needed 1)for sudo 2)for password 3)for retype password
		if (terminal.isWantToCreateAutoUser() && !alreadyTriedToCreateAutoUser) {
			alreadyTriedToCreateAutoUser = true;
			terminal.dontWantToCreateAutoUser();			
			// use the session to create the auto user
			String defaultUserName = Configuration.getInstance().getUser("default").getName();
			String autoUserName = Configuration.getInstance().getUser("auto").getName();
			// TODO this command should be in the config file and admin might be a better group to run sudo 
			CommandResult result = session.executeCommand(new Command("sudo /usr/sbin/useradd -G " + defaultUserName + ",sudo -K PASS_MAX_DAYS=-1 " + autoUserName));
			if (CommandStatus.FINISHED.equals(result.getCommandStatus())) {
				// just because it finished doesn't mean it worked e.g. useradd: group 'wheel' does not exist
				result = session.executeCommand(new Command("sudo /usr/bin/passwd " + autoUserName));
				if (CommandStatus.FINISHED.equals(result.getCommandStatus())) {
					destroySSHConnections();
					terminal = createPseudoTerminal();
					session = new Session(server, terminal, calledBy, testCommandTimeoutMillis);
				}
			}
			if (!CommandStatus.FINISHED.equals(result.getCommandStatus())) {
				logger.log(Level.SEVERE, "command did not finish when trying to create auto user: " +result.getOutput());
			}
		}
		allSessionPools.putSession(session.getSessionId(), session); // this calls back here to add it to sessions 
		return session;			
	}

	private synchronized PseudoTerminal createPseudoTerminal() {
		// find an SSHConnection with less than the maximum number of terminals
		SSHConnection connectionToUse = null;
		for (SSHConnection connectionWithTerminals : sshConnections) {
			if (connectionWithTerminals.getNumberOfTerminals() < MAX_TERMINALS_PER_SESSION) {
				connectionToUse = connectionWithTerminals;
				break;
			}
		}
		try {
			if (connectionToUse == null) {
				connectionToUse = new SSHConnection(server, this, sshConnections.size());
				sshConnections.add(connectionToUse);
			}
			return connectionToUse.createPseudoTerminal();
		} catch (Exception e) {
			destroySSHConnections();
			throw new MonitorRuntimeException(e);
		}
	}

	public void destroySSHConnections() {
		for (SSHConnection connection : sshConnections) {
			connection.destroy();
		}
		sshConnections = new ArrayList<SSHConnection>();
	}
	
	private int numberOfOpenSessions() {
		int count = 0;
		for (Session session : sessions) {
			if (session.isOpen()) {
				count++;
			}
		}
		return count;
	}	
	
	private synchronized Session findClosedSessionAndOpenIt(boolean killsubprocessesIfException) {
		for (Session existingSession : getLoggedOnSessions()) {
			if (!existingSession.isOpen() && existingSession.lockForOpening()) {
				try {
					// although we reset the ChunkedOutput when it was closed more output may have arrived
					//existingSession.flushInputFromShell();  // but ClosedSessionTests should have sorted this.
					existingSession.open();
					existingSession.unlockForOpening();
					return existingSession;
				} catch (Exception e) {
					existingSession.setKillSubprocesesWhenFinished(killsubprocessesIfException);
					existingSession.tidyUpAfterException("findClosedSessionAndOpenIt");
				}
			}
		}
		return null;
	}

	private List<Session> getLoggedOnSessions() {
		ArrayList<Session> loggedOn = new ArrayList<Session>();
		for (Session session : sessions) {
			if (session.isLoggedOn()) {
				loggedOn.add(session);
			}
		}
		return loggedOn;
	}	
	
	
	/** 
	 * A control session is needed to end jobs when other sessions are closed or logged out,
	 * they are not subject to the maximum sessions check and don't leave stray jobs. 
	 * It is important to get one otherwise all available session will get used up. 
	 * */
	public Session getControlSession(String calledBy) {
		Session session = findClosedSessionAndOpenIt(false);
		if (session == null) {
			int timeout = DEFAULT_TEST_COMMAND_TIMEOUT_MILLIS;
			int attempts = 0;
			while (attempts < 3 && session == null) {
				try {
					session = createSessionAndLogon(calledBy, timeout);
				} catch (Exception e) {
					attempts++;
					logger.log(Level.SEVERE, String.format("Failed attempt %d to get control session on %s", attempts, server.getHost()), e);
					session = null;
					timeout = timeout + DEFAULT_TEST_COMMAND_TIMEOUT_MILLIS;

				}
			}
			if (session == null) {
				throw new MonitorRuntimeException("Could not get a control session on" + server.getHost());
			}
		}
		session.markAsControlSession();
		return session;
	}	
	

	public List<Session> getSessionsByType(boolean includeFinished, SessionType...sessionType) {
		List<Session> sessionsOfSelectedTypes = new ArrayList<Session>();
		for (Session session : sessions) {
			if (includeFinished || CommandStatus.RUNNING == session.getLastCommandStatus()) {
				if (session.isLoggedOn() && session.isOpen() && !session.isControlSession()) {
					for (SessionType type : sessionType) {
						if (type == session.getSessionType()) {
							sessionsOfSelectedTypes.add(session);
						}
						break;
					}
				}
			}
		}
		return sessionsOfSelectedTypes;
	}


	public void removeSession(String sessionId) {
		for (int i=0; i < sessions.size(); i++) {
			if (sessions.get(i).getSessionId().equals(sessionId)) {
				sessions.remove(i);
			}
		}
	}

	/** called when all the terminals have been closed due to inactivity */
	public void removeSSHConnection(int indexInServerSessionPool) {
		sshConnections.remove(indexInServerSessionPool);
	}
	
	public List<SSHConnection> getSshConnections() {
		return sshConnections;
	}

	void add(Session session) {
		sessions.add(session);
	}


	public List<Session> getSessions() {
		return sessions;
	}
	
	/** for tests */
	public void setMaxSessions(int maxSessions) {
		ServerSessionPool.MAX_SESSIONS = maxSessions;
		tooManySessions = String.format(MAX_SESSIONS_ERROR, MAX_SESSIONS, this.server);
	}

	
}
