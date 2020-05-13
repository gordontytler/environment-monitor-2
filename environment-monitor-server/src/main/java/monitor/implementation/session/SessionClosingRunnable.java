package monitor.implementation.session;

import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import ch.ethz.ssh2.channel.ChannelClosedException;
import monitor.implementation.shell.CommandExecuter;
import monitor.model.CommandStatus;
import monitor.model.Server;

public class SessionClosingRunnable implements Runnable {

	static Logger logger = Logger.getLogger(SessionClosingRunnable.class.getName());	
	
	private static final int LONG_DELAY_BEFORE_INTERUPTING = 5000;
	private static final int SHORT_DELAY_BEFORE_INTERUPTING = 1000;

	private AllSessionPools allSessionPools = AllSessionPools.getInstance();	
	private ProcessKiller processKiller = new ProcessKiller();
	
	private Session session;
	private String calledBy;
	private CommandExecuter commandExecuter;
	private String previousSessionId;
	private boolean killSubprocesesWhenFinished; // an attribute of the session
	private Server server;
	private SessionIdMaker sessionIdMaker;
	private CountDownLatch latch; // caller can use this to wait for the run to complete

	
	public SessionClosingRunnable(Session session, String calledBy, CountDownLatch latch) {
		this.session = session;
		this.calledBy = calledBy;
		this.commandExecuter = session.getCommandExecuter();
		this.previousSessionId = session.getPreviousSessionId();		
		this.killSubprocesesWhenFinished = session.isKillSubprocesesWhenFinished();
		this.server = session.getServer();
		this.sessionIdMaker = session.getSessionIdMaker();
		this.latch = latch;
	}

	public void run() {
		close(calledBy);
		if (latch != null) {
			latch.countDown();
		}
	}

	
	 /** 
	  * Allows another task to use this logged in session to save the cost of creating a new ssh session.
	  * Closing the session kills child processes, changes the sessionId and clears its output. 
	  * If anything goes wrong we free up resources and don't re-add the session to the pool. 
	  */
	public void close(String calledBy) {
		String message = "success";
		try {
			if (session.isKillSubprocesesWhenFinished()) {
				// no point in killing sub-processes if it has finished unless we expect trouble
				// but always try to kill sub-processes if there was an error or the executor thinks they are running
				// e.g. tail server.log
				if (commandExecuter.getLastCommandStatus() == CommandStatus.RUNNING ||
						commandExecuter.getLastCommandStatus() == CommandStatus.ERROR ||
						(commandExecuter.getLastCommandStatus() == CommandStatus.FINISHED && killSubprocesesWhenFinished)) {
					// when close is done before logout by session reaper don't want to open another control session
					if (!session.isPreviouslyControlSession()) {
						String reason = String.format("lastCommandStatus: %s killSubprocesesWhenFinished: %b previouslyControlSession: %b", commandExecuter.getLastCommandStatus(), killSubprocesesWhenFinished, session.isPreviouslyControlSession());
						SessionEvent sessionEvent = new SessionEvent(System.currentTimeMillis(), "killSubProcesses", session.getSessionId(), session.isLoggedOn(), session.isOpen(), session.isControlSession(), session.getSessionType(), reason, session.getLastUsed());
						session.appendToSessionHistory(sessionEvent);
						processKiller.killSubProcesses(server, commandExecuter.getBashProcessId(), calledBy + "->SessionClosingRunnable.close");
					}
				}
			}
			session.prepareSessionForReuse(LONG_DELAY_BEFORE_INTERUPTING);
		} catch (Exception e) {
			message = e.getMessage();
			if (e instanceof ChannelClosedException) {
				logger.log(Level.WARNING, "ChannelClosedException when closing session for reuse. It will be removed from the pool. " + session.toString() + " Previous sessionId:" + previousSessionId);
				session.remove(message + " called by: " + calledBy);
			} else {
				logger.log(Level.SEVERE, "Failed to close session for reuse. It will be removed from the pool. " +
						session.toString() + " Previous sessionId:" + previousSessionId + " caused by: " + message);
				session.logout(message + " called by: " + calledBy);
				allSessionPools.dumpAllSessionsOnAllServers("to see if session.logout() due to previous exception removed " + session.toString());
			}
		}
		SessionEvent sessionEvent = new SessionEvent(System.currentTimeMillis(), "finished close", session.getSessionId(), session.isLoggedOn(), session.isOpen(), session.isControlSession(), session.getSessionType(), message, session.getLastUsed());
		session.appendToSessionHistory(sessionEvent);
	}

}
