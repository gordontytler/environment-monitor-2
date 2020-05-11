package monitor.implementation.session;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import monitor.model.Configuration;
import monitor.model.Server;

public class AllSessionPools {

	private static final int NBR_OLD_SESSION_IDS_TO_KEEP = 100000;
	static Logger logger = Logger.getLogger(AllSessionPools.class.getName());	
	private final String localHostName = Configuration.getInstance().getLocalHostName();
	private ServerSessionPoolFactory serverSessionPoolFactory = new ServerSessionPoolFactory();
	private ConcurrentHashMap<String, ServerSessionPool> allServerSessionPools = new ConcurrentHashMap<String, ServerSessionPool>();
	private ConcurrentHashMap<String, Session> allSessions = new ConcurrentHashMap<String, Session>();
	private ConcurrentHashMap<String, String> oldSessionIds = new ConcurrentHashMap<String, String>();
	private boolean shutdownThreadIsRunning = false;
	private static AllSessionPools theInstance = new AllSessionPools();
	
	public static AllSessionPools getInstance() {
		return theInstance;
	}
	
	private AllSessionPools() {
	}
	
	
	public synchronized ServerSessionPool getServerSessionPool(Server server) {
		ServerSessionPool serverSessionPool = allServerSessionPools.get(server.getHost());
		if (serverSessionPool == null) {
			serverSessionPool = serverSessionPoolFactory.makeServerSessionPool(server);
			allServerSessionPools.put(server.getHost(), serverSessionPool);
		}
		return serverSessionPool;
	}

	public Session getSessionUsingSessionId(String sessionId) {
		if (sessionId == null) {
			return null;
		}
		return allSessions.get(sessionId);
	}
	
	void putSession(String sessionId, Session session) {
		allSessions.put(sessionId, session);
		ServerSessionPool serverSessionPool = getServerSessionPool(session.getServer());
		serverSessionPool.add(session);
	}
	
	void removeSession(String sessionId) {
		Session session = allSessions.get(sessionId);
		if (session != null) {
			ServerSessionPool serverSessionPool = allServerSessionPools.get(session.getServer().getHost());
			if (serverSessionPool != null) {
				serverSessionPool.removeSession(sessionId);
			}
			allSessions.remove(sessionId);
		}
	}

	public boolean isShutdownThreadIsRunning() {
		return shutdownThreadIsRunning;
	}

	/** The shutdown hook uses this to quickly kill remote processes, log out and end connections.
	 * It leaves the connection pool in a mess and will result in errors if the JVM dooes not shut down and stuff continues to use it.
	 * @param calledBy monitor.api.Main#main
	 */
	public void messyLogoutAllSessionsOnAllServers(String calledBy) {
		shutdownThreadIsRunning = true;
		logoutAllSessionsOnAllServers(calledBy);
	}

	public void logoutAllSessionsOnAllServers(String calledBy) {
		logger.info(String.format("ShutdownHook: %s logging out %d remote ssh sessions.", Thread.currentThread().getName(), allSessions.size()));
		dumpAllSessionsOnAllServers(" because shutting down.");
		// First close all sessions where the last command has finished. This frees them up to be used as control sessions to kill processes.
		closeAllFinishedSessionsOnAllServers();
		// Now close them all. This will kill subprocesses.
		closeAllSessionsOnAllServers(calledBy + "AllSessionPools.logoutAllSessionsOnAllServers");
		// Now try to logout.
		for (Map.Entry<String, Session> entry : allSessions.entrySet()) {
			Session session = entry.getValue();
			if (session.isLoggedOn()) {
				session.logout("logoutAllSessionsOnAllServers called by: " + calledBy);
			}
		}
		if (allSessions.entrySet().size() > 0) {
			dumpAllSessionsOnAllServers("did not expect any sessions after logoutAllSessionsOnAllServers");
		}
		for (Map.Entry<String, ServerSessionPool> entry : allServerSessionPools.entrySet()) {
			ServerSessionPool serverSessionPool = entry.getValue();
			serverSessionPool.destroySSHConnections();
		}
		logger.info("Shutdown completed.\n\n\n");
		try {
			Thread.sleep(500);  // allow time for the last log entry to be written.
		} catch (InterruptedException e) {
		}
	}
	
	public void closeAllFinishedSessionsOnAllServers() {
		logger.info("closing all sessions where the last command has finished.");
		for (Map.Entry<String, Session> entry : allSessions.entrySet()) {
			Session session = entry.getValue();
			if (session.isLoggedOn() && session.isOpen()) {
				session.closeIfLastCommandFinished();
			}
		}
	}

	void closeAllSessionsOnAllServers(String calledBy) {
		logger.info("closing all sessions on all servers.");
		for (Map.Entry<String, Session> entry : allSessions.entrySet()) {
			Session session = entry.getValue();
			if (session.isLoggedOn() && session.isOpen()) {
				logger.info("closing " + session.toStatusString());
				session.close(calledBy + "->AllSessionPools.closeAllSessionsOnAllServers");
			}
		}
	}
	
	public String dumpAllSessionsOnAllServers(String reason) {
		StringBuilder sb = new StringBuilder("dump of all sessions " + reason + "\n");
		for (Map.Entry<String, Session> entry : allSessions.entrySet()) {
			Session session = entry.getValue();
			sb.append(session.toStatusString());
		}
		logger.info(sb.toString());
		return sb.toString();
	}
	
	ConcurrentHashMap<String, Session> getAllSessions() {
		return allSessions;
	}

	public ConcurrentHashMap<String, ServerSessionPool> getAllServerSessionPools() {
		return allServerSessionPools;
	}
	
	/** Convenient method to get a session on the local machine where environment-monitor is running. */
	public Session getLocalSession(String calledBy) {
		Server server = new Server(localHostName);
		ServerSessionPool ssp = getServerSessionPool(server);
		Session session = ssp.getSession(calledBy);
		session.setKillSubprocesesWhenFinished(false);
		return session;
	}
	
	
	// for tests
	public void setServerSessionPoolFactory(
			ServerSessionPoolFactory serverSessionPoolFactory) {
		this.serverSessionPoolFactory = serverSessionPoolFactory;
	}

	public void setAllServerSessionPools(ConcurrentHashMap<String, ServerSessionPool> allServerSessionPools) {
		this.allServerSessionPools = allServerSessionPools;
	}

	public void saveOldSessionId(String previousSessionId, String sessionId) {
		oldSessionIds.put(previousSessionId, sessionId);
		int id = Integer.parseInt(sessionId);
		if (oldSessionIds.size() > NBR_OLD_SESSION_IDS_TO_KEEP && id % 500 == 0) {
			new Thread(new OldSessionMapLimiter(), "OldSessionMapLimiter").run();
		}
	}

	private class OldSessionMapLimiter implements Runnable {

		public void run() {
			int oldSize = oldSessionIds.size();
			Set<String> keySet = oldSessionIds.keySet();
			ArrayList<String> keys = new ArrayList<String>(oldSessionIds.size());
			keys.addAll(keySet);
			Collections.sort(keys);
			for (int i=0; i < oldSize - NBR_OLD_SESSION_IDS_TO_KEEP; i++) {
				oldSessionIds.remove(keys.get(i));
			}
			logger.info("Size of oldSessionsIds map reduced from " + oldSize + " to " + oldSessionIds.size() );			
		}
		
	}
	
	public String findReplacementSessionId(String originalSessionId) {
		String original = oldSessionIds.get(originalSessionId);
		String next = original;
		while (next != null) {
			original = next;
			next = oldSessionIds.get(original);
		}
		return original;
	}
	
}
