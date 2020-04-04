package monitor.implementation.session;

import java.io.InputStream;
import java.io.OutputStream;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;

public class PseudoTerminal implements SessionIdMaker {
	
	private SessionIdMaker nextSessionId = new NextSessionId();
	private String sessionId = nextSessionId.makeNewSessionId();
	private Session sess;
	private InputStream in;
	private OutputStream out;
	private SSHConnection sshConnection;


	public PseudoTerminal(SSHConnection sshConnection) throws Exception {
		this.sshConnection = sshConnection;
		Connection conn = sshConnection.getConnection();
		sess = conn.openSession();
		sess.requestPTY("dumb", 150, 24, 0, 0, null);
		sess.startShell();
		in = sess.getStdout();
		out = sess.getStdin();
	}

	public InputStream getStdout() {
		return in;
	}

	public OutputStream getStdin() {
		return out;
	}

	public String getSessionId() {
		return sessionId;
	}
	
	/** To prevent the old client from using the session when it has been reallocated. */
	@Override
	public String makeNewSessionId() {
		sshConnection.removePseudoTerminalButKeepConnectionOpen(this);
		sessionId = nextSessionId.makeNewSessionId();
		sshConnection.addPseudoTerminal(this);
		return sessionId;
	}
	
	/** close the session and inform the SSHConnection that there is one less PseudoTerminal using it. */
	public void destroy() {
		if (sess != null) {
			sess.close();
		}
		sshConnection.removePseudoTerminal(this);
	}
	
	public String getLoggedOnUserName() {
		return sshConnection.getLoggedOnUserName();
	}

	public String getLoggedOnUserPassword() {
		return sshConnection.getLoggedOnUserPassword();
	}

	public boolean isWantToCreateAutoUser() {
		return sshConnection.isWantToCreateAutoUser();
	}

	/** The attempt either suceeded or failed. */
	public void dontWantToCreateAutoUser() {
		sshConnection.setWantToCreateAutoUser(false);
	}
	
	@Override
	public String toString() {
		return sessionId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sessionId == null) ? 0 : sessionId.hashCode());
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
		PseudoTerminal other = (PseudoTerminal) obj;
		if (sessionId == null) {
			if (other.sessionId != null)
				return false;
		} else if (!sessionId.equals(other.sessionId))
			return false;
		return true;
	}

	
}
