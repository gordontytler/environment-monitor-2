package monitor.implementation.session;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import monitor.implementation.action.AlreadyRunningInfo;
import monitor.implementation.shell.ChunkedOutput;
import monitor.implementation.shell.CommandExecuter;
import monitor.implementation.shell.CommandExecuterFactory;
import monitor.model.Action;
import monitor.model.Command;
import monitor.model.CommandResult;
import monitor.model.CommandStatus;
import monitor.model.Configuration;
import monitor.model.RingBuffer;
import monitor.model.Server;
import monitor.model.SessionType;
import monitor.model.StringUtil;
import monitor.model.User;
import ch.ethz.ssh2.Connection;

/**
 * A session represents the execution of a remote bash shell and manages its state so that it can
 * be returned to a pool and re-used later. A session controls the remote program via a {@link CommandExecuter}. 
 */
public class Session {

	private static final int LONG_DELAY_BEFORE_INTERUPTING = 5000;
	static Logger logger = Logger.getLogger(Session.class.getName());
	private CommandExecuterFactory commandExecuterFactory = new CommandExecuterFactory();
	private Server server;
	private SessionIdMaker sessionIdMaker;
	protected boolean loggedOn = false;
	boolean open = false;	// if logged on and closed the session is available to reuse
	private ReentrantReadWriteLock openingReadWriteLock = new ReentrantReadWriteLock();
	private CommandExecuter commandExecuter;
	private PseudoTerminal terminal = null;
	private SessionType sessionType = SessionType.TERMINAL;
	protected long lastUsed = System.currentTimeMillis();
	private long lastTested;
	
	private AllSessionPools allSessionPools = AllSessionPools.getInstance();
	private ProcessKiller processKiller = new ProcessKiller();
	private boolean controlSession; // used to kill processes
	private boolean previouslyControlSession = false; // used to kill processes	
	private String environmentName; // scripts are passed a sessionId. Info needed when they call back is held in the session.
	private String calledBy; // vital if you want to debug sessions not being closed
	private boolean killSubprocesesWhenFinished = true;

	private Action action;
	private String sessionId;
	private String previousSessionId = "none";
	private AlreadyRunningInfo alreadyRunningInfo;
	private static ExecutorService sessionClosingPool = Executors.newFixedThreadPool(20);
	
	private RingBuffer<SessionEvent> sessionHistory = new RingBuffer<SessionEvent>(1000);

	
	/** Test constructor which allows you to inject a mock commandExecuterFactory before calling one of the initialise methods. */
	public Session() {
	}

	/** Use the pure Java ssh implementation from http://www.cleondris.ch/opensource/ssh2/<br> 
	 * The protocol architecture implemented by the library is documented in RFC 4251.*/
	public Session(Server server, PseudoTerminal terminal, String calledBy, int testCommandTimeoutMillis) {
		initialiseSSHExecuter(server, terminal, calledBy, testCommandTimeoutMillis);
		sessionHistory.append(new SessionEvent(System.currentTimeMillis(), "create", sessionId, loggedOn, open, controlSession, sessionType, calledBy, lastUsed));
	}	
	
	void initialiseSSHExecuter(Server server, PseudoTerminal terminal, String calledBy, int testCommandTimeoutMillis) {
		this.server = server;
		this.terminal = terminal;
		this.sessionIdMaker = terminal;
		this.sessionId = terminal.getSessionId();		
		this.calledBy = calledBy;
		commandExecuter = commandExecuterFactory.createSSHExecuter(server, terminal, testCommandTimeoutMillis);
		open();
	}

	/** 
	 * Runs openSSH in a separate process. Deprecated mainly due to poor performance.
	 * Despite this it is  worth keeping because a new Java version may resolve the performance problems
	 * and it is useful for diagnosing ssh problems. The alternative is unsupported open source beta code that 
	 * may stop being free or stop working if openssh changes.
	 * <p>
	 * Issues with the process based BashExecuter are... 
	 * <ul>
	 *  <li>the 'process reaper' thread started by UNIXprocess.java will eat your cpu cycles
	 *  <li>there is IO between the this process and the local ssh process and also to the remote shell 
	 *  <li>more cpu is wasted to get round the lack of a pseudo-terminal with a prompt showing that the previous command has finished
	 *  <li>no control over authentication
	 *  <li>not a login session (the /home/user/.bashrc does not run)
	 *  <li>only runs on Linux
	 *  <li>processes can be left running on the local and remove machine 
	 * <ul>
	 *  @deprecated use {@link Session#SSHSession(Server, Connection, String)} instead
	 **/	
	public Session(Server server, NextSessionId nextSessionId, String calledBy) {
		initialiseBashExecuter(server, nextSessionId, calledBy);
	}	

	void initialiseBashExecuter(Server server, NextSessionId nextSessionId, String calledBy) {
		this.server = server;
		this.calledBy = calledBy;
		this.sessionIdMaker = nextSessionId;
		sessionId = nextSessionId.makeNewSessionId();		
		User user = Configuration.getInstance().getUser(server.getHost());
		commandExecuter = commandExecuterFactory.createBashExecuter(server, sessionId, user);
		open();
	}
	
	/** Check that a terminal is still working when it is to be reused after it has been closed but left logged on. */
	public void testTerminal() throws Exception {
		commandExecuter.testTerminal();
	}

	public void setKillSubprocesesWhenFinished(boolean killWhenFinished) {
		this.killSubprocesesWhenFinished = killWhenFinished;
	}

	public void tidyUpAfterException(String calledBy) {
		sessionHistory.append(new SessionEvent(System.currentTimeMillis(), "testTerminal", sessionId, loggedOn, open, controlSession, sessionType, "tidyUpAfterException called by: " + calledBy, lastUsed));
		if (killSubprocesesWhenFinished) {
			try {
				processKiller.killSubProcesses(server, commandExecuter.getBashProcessId(), calledBy + "->Session.tidyUpAfterException");
			} catch (Exception e) {
				logger.log(Level.INFO, String.format("Problem closing session while tidying up - %s", this.toString()), e);
			}
		}
		try {
			logout("tidyUpAfterException called by: " + calledBy);
		} catch (Exception e) {
			logger.log(Level.INFO, String.format("Problem with logout while tidying up after error - %s", this.toString()), e);
		}
	}
	
	void logout(String reason) {
		String message = "success";
		try {
			loggedOn = false;
			open = false;
			// killProcess calls PseudoTerminal.distroy which calls SSHConnection.removePseudoTerminal 
			// which calls ServerSessionPool.removeSSHConnection when there are no terminals left.  
			commandExecuter.killProcess();
		} catch (Exception e) {
			message = String.format("%s happened when trying to destroy sessionId:%s on %s with PID %d.", e.getMessage(), sessionId, server.getHost(), commandExecuter.getBashProcessId());
			logger.log(Level.INFO, message);
		}
		sessionHistory.append(new SessionEvent(System.currentTimeMillis(), "logout", sessionId, loggedOn, open, controlSession, sessionType, reason, lastUsed));
		allSessionPools.removeSession(getSessionId());		
	}

	 /** 
	  * Allows another task to use this logged in session to save the cost of creating a new ssh session.
	  * Closing the session kills child processes, changes the sessionId and clears its output. 
	  * If anything goes wrong we free up resources and don't re-add the session to the pool.
	  * 
	  * This is done in a separate thread so as to not block the caller.
	  */
	public CountDownLatch close(String calledBy) {
		sessionHistory.append(new SessionEvent(System.currentTimeMillis(), "close", sessionId, loggedOn, open, controlSession, sessionType, "starting thread to close session. Called by: " + calledBy, lastUsed));
		CountDownLatch latch = new CountDownLatch(1);
		sessionClosingPool.execute(new SessionClosingRunnable(this, calledBy, latch));
		return latch;
	}

	void appendToSessionHistory(SessionEvent sessionEvent) {
		sessionHistory.append(sessionEvent);
	}
	
	public RingBuffer<SessionEvent> getSessionHistory() {
		return sessionHistory;
	}
	
	/** This frees up the session to be used as a control session to kill processes when shutting down. 
	 * It should only be called by AllSessionPools.closeAllFinishedSessionsOnAllServers */
	public void closeIfLastCommandFinished() {
		if (commandExecuter.getLastCommandStatus() == CommandStatus.FINISHED) {
			resetAfterClosing();
		}
	}

	void resetAfterClosing() {
		action = null;
		sessionType = SessionType.TERMINAL;
		previouslyControlSession = controlSession;		
		controlSession = false;
		killSubprocesesWhenFinished = true;
		open = false;
	}
	
	public void makeLoggedOnMessage() {
		appendToChunkedOutput(String.format("User %s logged on to %s PID %d sessionId %s\n", getLoggedOnUserName(), server.getHost(), commandExecuter.getBashProcessId(), sessionId));
	}	

	/** 
	 * @return {@code true} if the lock was free and was acquired by the current thread.
     */
	public boolean lockForOpening() {
		return openingReadWriteLock.writeLock().tryLock();
	}
	
	public void unlockForOpening() {
		openingReadWriteLock.writeLock().unlock();
	}
	
	/** Indicate that this session is in use. */
	void open() {
		this.open = true;
		this.loggedOn = true;
		sessionHistory.append(new SessionEvent(System.currentTimeMillis(), "open", sessionId, loggedOn, open, controlSession, sessionType, "", lastUsed));
	}	
	
	public CommandResult killRunningCommand(String calledBy) throws Exception {
		sessionHistory.append(new SessionEvent(System.currentTimeMillis(), "killRunningCommand", sessionId, loggedOn, open, controlSession, sessionType, "Called by: " + calledBy, lastUsed));		
		int chunkBefore = commandExecuter.getChunkedOutput().getHighestChunkNumber();
		commandExecuter.stopReadingInput();
		processKiller.killSubProcesses(server, commandExecuter.getBashProcessId(), calledBy + "->Session.killRunningCommand");
		CommandResult commandResult = new CommandResult();
		commandResult.setSessionId(getSessionId());
		commandResult.setCommandStatus(CommandStatus.FINISHED);
		commandResult.setOutput(commandExecuter.getChunkedOutput().getChunk(chunkBefore + 1));
		if (SessionType.ACTION == sessionType) {
			new SessionClosingRunnable(this, "Session.closeIfLastCommandFinished", null).prepareSessionForReuse(LONG_DELAY_BEFORE_INTERUPTING);
			commandResult.setSessionId(null);
		}
		return commandResult;
	}
	
	public CommandResult executeCommand(Command command) {
		if (!controlSession) {
			lastUsed = System.currentTimeMillis();
		}
		CommandResult commandResult = commandExecuter.executeCommand(command);
		sessionHistory.append(new SessionEvent(System.currentTimeMillis(), "executeCommand", sessionId, loggedOn, open, controlSession, sessionType, 
				String.format("%s %s %s", command.getRequest(), commandResult.getCommandStatus(), StringUtil.safeSubString(commandResult.getOutput(), 120)), lastUsed));
		return commandResult;
	}

	public void appendToChunkedOutput(String chunk) {
		lastUsed = System.currentTimeMillis();
		commandExecuter.appendToChunkedOutput(chunk);
	}
	
	public CommandStatus getLastCommandStatus() {
		return commandExecuter.getLastCommandStatus();
	}

	public void setLastCommandStatus(CommandStatus commandStatus) {
		commandExecuter.setLastCommandStatus(commandStatus);
	}
	
	
	public String getRunningCommand() {
		return commandExecuter.getRunningCommand();
	}
	
	public ChunkedOutput getChunkedOutput() {
		lastUsed = System.currentTimeMillis();
		return commandExecuter.getChunkedOutput();
	}

	public void copyOutputTo(ChunkedOutput sshSessionOutput) {
		commandExecuter.copyOutputTo(sshSessionOutput);
	}
	
	public boolean isOpen() {
		return open;
	}
	
	public boolean isLoggedOn() {
		return loggedOn;
	}

	public boolean isControlSession() {
		return controlSession;
	}
	
	public void markAsControlSession() {
		controlSession = true;
		killSubprocesesWhenFinished = false;
	}

	public String getSessionId() {
		return sessionId;
	}

	public Server getServer() {
		return server;
	}

	public SessionType getSessionType() {
		return sessionType;
	}

	public void setSessionType(SessionType sessionType) {
		this.sessionType = sessionType;
	}

	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action; 
	}

	public int getBashProcessId() {
		return commandExecuter.getBashProcessId();
	}
	
	public void setProcessKiller(ProcessKiller processKiller) {
		this.processKiller = processKiller;
	}

	public long getLastUsed() {
		return lastUsed;
	}
	
	public void updateLastUsed() {
		lastUsed = System.currentTimeMillis();
	}
	
	public long getLastTested() {
		return lastTested;
	}

	public void updateLastTested() {
		lastTested = System.currentTimeMillis();
	}
	
	public void setEnvironmentName(String environmentName) {
		this.environmentName = environmentName;
	}
	
	public String getEnvironmentName() {
		return environmentName;
	}

	/** String showing chain of calls leading to the Session constructor. */
	public String getCalledBy() {
		return calledBy;
	}

	public CommandExecuter getCommandExecuter() {
		return commandExecuter;
	}

	/** The deepest command in the process tree after the continuous output started. */
	public void setAlreadyRunningInfo(AlreadyRunningInfo info) {
		this.alreadyRunningInfo = info;
	}

	public AlreadyRunningInfo getAlreadyRunningInfo() {
		return alreadyRunningInfo;
	}

	public String getLoggedOnUserName() {
		if (terminal == null) {
			return "";
		} else {
			return terminal.getLoggedOnUserName();
		}
	}
	
	void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}	

	void setPreviousSessionId(String previousSessionId) {
		this.previousSessionId = previousSessionId;
	}

	public String getPreviousSessionId() {
		return previousSessionId;
	}

	public boolean isKillSubprocesesWhenFinished() {
		return killSubprocesesWhenFinished;
	}

	public SessionIdMaker getSessionIdMaker() {
		return sessionIdMaker;
	}
	
	public boolean isPreviouslyControlSession() {
		return previouslyControlSession;
	}

	/** for tests */
	public void setCommandExecuterFactory(CommandExecuterFactory commandExecuterFactory) {
		this.commandExecuterFactory = commandExecuterFactory;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((server == null) ? 0 : server.hashCode());
		result = prime * result + sessionId.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return String.format("sessionId:%s PID %d on %s", sessionId, getBashProcessId(), server);
	}

	
	public String toStatusString() {
		return String.format("sessionId:%-6s %-5s on %-35s loggedOn: %-5b open: %-5b controlSession: %-5b %-8s %-8s %s %s\n", 
			getSessionId(), getLoggedOnUserName(), getServer(), isLoggedOn(), isOpen(), isControlSession(), 
			getLastCommandStatus(), getSessionType(), getRunningCommand(), getCalledBy());
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Session other = (Session) obj;
		if (server == null) {
			if (other.server != null)
				return false;
		} else if (!server.equals(other.server))
			return false;
		if (!sessionId.equals(other.sessionId))
			return false;
		return true;
	}


	
	/* =============================================================
	 * This chunk is from a failed attempt to open a process with bash as the program and then run ssh as a command in bash.
	 * It turned out much better to run ssh directly
	 * =============================================================
	
	Command command = new Command(String.format("ssh %s@%s", user.getName(), server.getHost()));
	command.setMillisBeforeTimeout(5000);
	response = bashExecuter.executeQuickCommand(command);
	logger.info(response);
	if (response.endsWith("Are you sure you want to continue connecting (yes/no)? ")) {
		response = bashExecuter.executeQuickCommand(new Command("yes"));
	}
	if (response.endsWith("password: ")) {
		response = bashExecuter.executeQuickCommand(new Command(user.getPassword()));
	} else if (response.length() > 0) {
		// we probably want to check a list of acceptable responses here
		if ("Agent admitted failure to sign using the key.".trim().equals(response)) {
			
		} else {
			throw new MonitorRuntimeException(String.format("Unexpected response from login to %s : %s",  server.getHost(), response));	
		}
	}
	
		//response = bashExecuter.executeQuickCommand(new Command("set -m")); // bash : no job control in this shell
		//Command command = new Command(String.format("ssh -nN %s@%s", user.getName(), server.getHost()));
	
	*/
	
	
	
	/*
	 ================================================================================
	 How to copy public keys to the server so that the password is not needed.
	 ================================================================================
	 
	 You cant make openssh ask for the password from a redirected standard input. 
	 
	 With Ubuntu
	
	 with -s  the ssh login pops up a window when JUnit or main class is run from eclipse
	 with -si or just -i ssh causes bash : no job control in this shell
	 with just -s and running java from a terminal it no longer pops up a window but 
	 instead requests the password from the terminal. This causes the java to crash because it expects the 
	 request for the password to go to the inputStream so it kills the bash process and leaves the terminal 
	 with the keyboard no longer being echoed.
	
	 I get exactly the same thing if ssh is the initial program - an OpenSSH window pops up and asks for the password.
	
	 cd ~/eclipse/workspace-three/environment-monitor/bin
	 /usr/java/jre1.6.0_17/bin/java monitor/implementation/ServerSessionPoolTest &
	
	 If the above is run in background it still prompts.		
	
	 if ssh is used instead of bash as the program to run in the process the same problems occur.
	
	 I give up.  It looks like openssh is designed to always take the password from a keyboard even when stdin is redirected.
	
	 see http://www.debian-administration.org/articles/152
	
	 ssh-keygen -t rsa                                     # creates my public and private rsa keys
	 ssh-copy-id -i ~/.ssh/id_rsa.pub username@mystery     # copys my public key to the host
	
	 On the host my public key goes in ~/.ssh/authorized_keys
	
	 An experiment shows I can I have more than one public/private pair on the host, so, as long as I know the password all I have to do is
	 generate my keys and do this ssh-copy-id to each host.
	
	*/
	
	

	
	/*  Another example..... the password has to be added when doing the ssh-copy-id but dont see how it can pop up a window


[devops@node421 ~]$ ssh-keygen -t rsa


Generating public/private rsa key pair.
Enter file in which to save the key (/home/devops/.ssh/id_rsa):
Enter passphrase (empty for no passphrase): 
Enter same passphrase again: 
Your identification has been saved in /home/devops/.ssh/id_rsa.
Your public key has been saved in /home/devops/.ssh/id_rsa.pub.
The key fingerprint is:
f3:13:72:31:4e:a5:58:cc:02:c5:68:66:71:a9:26:5a devops@node421.test.carrero.es



[devops@node421 ~]$ ssh-copy-id -i ~/.ssh/id_rsa.pub devops@node528.test.carrero.es

27
The authenticity of host 'node528.test.carrero.es (10.160.146.128)' can't be established.
RSA key fingerprint is 99:93:5c:96:45:f1:11:95:1f:d7:87:ed:ed:0b:52:6f.
Are you sure you want to continue connecting (yes/no)? yes
Warning: Permanently added 'node528.test.carrero.es,10.160.146.128' (RSA) to the list of known hosts.
devops@node528.test.carrero.es's password:
Now try logging into the machine, with "ssh 'devops@node528.test.carrero.es'", and check in:

  .ssh/authorized_keys

to make sure we haven't added extra keys that you weren't expecting.


 
[devops@node421 ~]$ ssh devops@node528.test.carrero.es


Last login: Fri Feb  5 17:16:53 2010 from 10.2.5.61
[devops@node528 ~]$  # I didnt have to enter a password.
[devops@node528 ~]$
[devops@node528 ~]$ vi .ssh/authorized_keys

ssh-rsa AAAAB3NzaC1yc2EAAAABIwAAAQEAwbVz8u/vmo5Z3Il59yh+w0Ndezsj2RUZBbTdMIDcE3jdqz2fwAT75GfWteJ/n98eUhkRkMOdIxQg0zvCWepAL1YFEsrVjz5O2sYT1JduYc5KOE6APUAf8CWoVl3AljySv+69mrWMkLJELgBWPm7XgmdYPh3i45ioaQ5FW8Tt4CVDK8U8Cj3zLt7nub/BJCZrFt8atAWIJ7ymqWWDgFuLy57gMrfuJAArE4AfuvDW8XwWa5CsmiZnn0AzU6gLTw2miEqqxxuoECsgSJilglBL4YLRDCI46z8wW6Fa9dzRrO1W4i86JuUSr1wywjKWCRbsGNVOHlpx5neEJIG4D4jTXQ== devops@node421.test.carrero.es

 
	 */	
	
}
