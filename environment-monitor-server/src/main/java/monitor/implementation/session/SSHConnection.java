package monitor.implementation.session;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import monitor.implementation.MonitorRuntimeException;
import monitor.model.Configuration;
import monitor.model.Server;
import monitor.model.User;
import ch.ethz.ssh2.Connection;

public class SSHConnection {

	static Logger logger = Logger.getLogger(SSHConnection.class.getName());	
	
	private Server server;
	private int indexInServerSessionPool;	
	private Connection conn;
	private String username = System.getenv().get("USER");   // alternatives LOGNAME , USERNAME
	private File keyfile = new File("/home/" + username + "/.ssh/id_rsa"); 
	private String keyfilePass = "?"; // will be ignored if not needed	

	private String loggedOnUserName;
	private String loggedOnUserPassword;
	
	List<PseudoTerminal> terminals = new ArrayList<PseudoTerminal>();

	private boolean wantToCreateAutoUser = false;

	private ServerSessionPool serverSessionPool;


	/**
	 * First try to connect with the password for the user of the server we are connecting to.
	 * If the config doesn't have this use the password for the default user instead.
	 * If password authentication fails try public key authentication using the public key for the
	 * user who is running this program.
	 * 
	 * @param server
	 * @param serverSessionPool 
	 * @throws Exception
	 */
	public SSHConnection(Server server, ServerSessionPool serverSessionPool, int indexInServerSessionPool) throws Exception {
 		this.server = server;
 		this.serverSessionPool = serverSessionPool;
 		this.indexInServerSessionPool = indexInServerSessionPool;
		conn = new Connection(server.getHost());
		conn.connect();
		//This program has tidy up threads so conn.addConnectionMonitor(cmon) is not needed
		
		User user = null;
		boolean isAuthenticated = false;
		boolean loggedOnAsAuto = false;
		StringBuilder trace = new StringBuilder("server: ").append(server.getHost()).append(" ");

		try {
			// First try to log on as auto user
			if (Configuration.getInstance().isAutoTryFirst()) {
				trace.append("trying to logon as auto. ");
				user = Configuration.getInstance().getUser("auto");
				isAuthenticated = tryToLogonWithPassword(server, user, trace);
				if (isAuthenticated) {
					loggedOnAsAuto = true;
				} else {
					// Need to close the connection before trying to log on as a different user Caused by: java.io.IOException: Peer sent DISCONNECT message (reason code 2): Change of username or service not allowed: (auto,ssh-connection) -> (devops,ssh-connection)
					conn.close();
					conn = new Connection(server.getHost());
					conn.connect();					
				}
			}
			if (!isAuthenticated) {
				// Then try the specific user for the host or the default
				user = Configuration.getInstance().getUser(server.getHost());
				trace.append("trying to logon as ").append(user.getName()).append(". ");
				isAuthenticated = tryToLogonWithPassword(server, user, trace); 
				if (!isAuthenticated) {
					// As a last resort try public key authentication
					trace.append("trying public key authentication ");
					conn.close();
					conn = new Connection(server.getHost());
					conn.connect();
					// TODO - Get the correct public key from the user's home directory.
					//
					// Won't this be the wrong username unless connection is to localhost? Perhaps.
					// The public keys are in the home directories of the users of monitor server.
					// Need to confirm this but basically a remote server just checks that the public key you are
					// sending it is the same as the the one it already has? If so, we don't need to create these users
					// on monitor server. We just need the public keys?
					//
					// What's wrong with user.getName ?
					// isAuthenticated = conn.authenticateWithPublicKey(username, keyfile, keyfilePass);
					isAuthenticated = conn.authenticateWithPublicKey(user.getName(), keyfile, keyfilePass);
					if (isAuthenticated) {
						loggedOnUserName = user.getName();
						logger.info(String.format("logged on to server %s as user %s using public key authentication.", server.getHost(), username));
					} else {
						throw new MonitorRuntimeException(String.format("\n\npassword authentication failed for user %s and public key authentication failed for user %s on server %s using keyfile %s\n\n", user.getName(), username, server.getHost(), keyfile.toString()));
					}
				}
			}
			if (!loggedOnAsAuto && Configuration.getInstance().isAutoCreate()) {
				// can't create the user yet, we need a Session and an SSHExecuter first 
				wantToCreateAutoUser = true;
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, trace.toString(), e);
			throw new MonitorRuntimeException(e);
		}
	}

	private boolean tryToLogonWithPassword(Server server, User user, StringBuilder trace) throws IOException {
		trace.append("trying main password ");
		boolean isAuthenticated = conn.authenticateWithPassword(user.getName(), user.getPassword());
		if (isAuthenticated) {
			loggedOnUserName = user.getName();
			loggedOnUserPassword = user.getPassword();
			logger.info(String.format("logged on to server %s as user %s using password authentication.", server.getHost(), user.getName()));
		} else {
			trace.append("trying other password ");
			isAuthenticated = conn.authenticateWithPassword(user.getName(), user.getOtherPassword());
			if (isAuthenticated) {
				loggedOnUserName = user.getName();
				loggedOnUserPassword = user.getOtherPassword();
				logger.info(String.format("Two attempts were needed to log on to server %s as user %s using password authentication.", server.getHost(), user.getName()));
			}
		}
		return isAuthenticated;
	}

	Connection getConnection() {
		return conn;
	}

	public void destroy() {
		conn.close();
	}
	
	void removePseudoTerminal(PseudoTerminal pseudoTerminal) {
		terminals.remove(pseudoTerminal);
		if (terminals.isEmpty()) {
			if (serverSessionPool != null) {
				// todo bug java.lang.IndexOutOfBoundsException: Index: 1, Size: 1  in  ServerSessionPoolTest.testGetSession
				serverSessionPool.removeSSHConnection(indexInServerSessionPool);
			}
			destroy();
		}
	}

	void removePseudoTerminalButKeepConnectionOpen(PseudoTerminal pseudoTerminal) {
		terminals.remove(pseudoTerminal);
	}	
	
	int getNumberOfTerminals() {
		return terminals.size();
	}

	public PseudoTerminal createPseudoTerminal() throws Exception {
		PseudoTerminal terminal = new PseudoTerminal(this); 
		terminals.add(terminal);
		return terminal;
	}
	
	void addPseudoTerminal(PseudoTerminal pseudoTerminal) {
		terminals.add(pseudoTerminal);
	}
	
	public String getLoggedOnUserName() {
		return loggedOnUserName;
	}

	String getLoggedOnUserPassword() {
		return loggedOnUserPassword;
	}

	public boolean isWantToCreateAutoUser() {
		return wantToCreateAutoUser;
	}

	public void setWantToCreateAutoUser(boolean wantToCreateAutoUser) {
		this.wantToCreateAutoUser = wantToCreateAutoUser;
	}

	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((conn == null) ? 0 : conn.hashCode());
		result = prime * result + ((keyfile == null) ? 0 : keyfile.hashCode());
		result = prime * result
				+ ((keyfilePass == null) ? 0 : keyfilePass.hashCode());
		result = prime
				* result
				+ ((loggedOnUserName == null) ? 0 : loggedOnUserName.hashCode());
		result = prime
				* result
				+ ((loggedOnUserPassword == null) ? 0 : loggedOnUserPassword
						.hashCode());
		result = prime * result + ((server == null) ? 0 : server.hashCode());
		result = prime
				* result
				+ ((serverSessionPool == null) ? 0 : serverSessionPool
						.hashCode());
		result = prime * result
				+ ((terminals == null) ? 0 : terminals.hashCode());
		result = prime * result
				+ ((username == null) ? 0 : username.hashCode());
		result = prime * result + (wantToCreateAutoUser ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SSHConnection other = (SSHConnection) obj;
		if (conn == null) {
			if (other.conn != null)
				return false;
		} else if (!conn.equals(other.conn))
			return false;
		if (keyfile == null) {
			if (other.keyfile != null)
				return false;
		} else if (!keyfile.equals(other.keyfile))
			return false;
		if (keyfilePass == null) {
			if (other.keyfilePass != null)
				return false;
		} else if (!keyfilePass.equals(other.keyfilePass))
			return false;
		if (loggedOnUserName == null) {
			if (other.loggedOnUserName != null)
				return false;
		} else if (!loggedOnUserName.equals(other.loggedOnUserName))
			return false;
		if (loggedOnUserPassword == null) {
			if (other.loggedOnUserPassword != null)
				return false;
		} else if (!loggedOnUserPassword.equals(other.loggedOnUserPassword))
			return false;
		if (server == null) {
			if (other.server != null)
				return false;
		} else if (!server.equals(other.server))
			return false;
		if (serverSessionPool == null) {
			if (other.serverSessionPool != null)
				return false;
		} else if (!serverSessionPool.equals(other.serverSessionPool))
			return false;
		if (terminals == null) {
			if (other.terminals != null)
				return false;
		} else if (!terminals.equals(other.terminals))
			return false;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		if (wantToCreateAutoUser != other.wantToCreateAutoUser)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "host: " + server + " terminals: " + terminals;
	}
}
