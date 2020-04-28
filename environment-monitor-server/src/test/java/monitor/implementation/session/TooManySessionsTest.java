package monitor.implementation.session;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.InputStream;
import java.util.logging.Logger;

import monitor.model.Configuration;
import monitor.model.Server;

import org.junit.After;
import org.junit.Test;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;

/**
 * I cannot open more than 10 concurrent sessions (or SCP clients).
 * <p>
 * You are probably using OpenSSH. By looking at their source code you will find
 * out that there is a hard-coded constant called MAX_SESSIONS in the session.c
 * file which is set to "10" by default. This is a per connection limit.
 * Unfortunately, it is not a run-time tunable parameter. However, this limit
 * has no effect on the number of concurrent port forwardings. Please note: this
 * information is based on the OpenSSH 4.3 release.
 * <p>
 * Possible solutions:
 * <ul>
 * <li>(a) Recompile your SSH daemon 
 * <li>(b) Try to live with this limit and keep the number of concurrent sessions <= 10. 
 * <li>(c) Distribute your sessions over multiple concurrent SSH connections.
 * </ul>
 * <p>
 * Just for completeness: starting from release 210, the thrown exception may
 * look as follows:
 * <p>
 * java.io.IOException: Could not open channel (The server refused to open the channel (SSH_OPEN_ADMINISTRATIVELY_PROHIBITED, 'open failed'))
 * <p>
 * see {@link http://www.cleondris.ch/opensource/ssh2/FAQ.html#env}
 * 
 * @author Christian Plattner
 * 
 */
public class TooManySessionsTest {
	
	static Logger logger = Logger.getLogger(SSHSessionTest.class.getName());

	private byte[] buff = new byte[8192];  // 2^13 or 2000 hex.	
	static String host = Configuration.getInstance().getDefaultHostForTests();
	static Server server = new Server(host);
	String username = Configuration.getInstance().getUser(host).getName();
	File keyfile = new File("/home/" + username + "/.ssh/id_rsa"); 
	String keyfilePass = "?"; // will be ignored if not needed
	
	static Connection conn;
	
	int sessionCount = 0;

	@After
	public void tearDown() {
		conn.close();
	}

	@Test
	public void testTooManySessions() throws Exception {
		System.out.println("_________________________________________________________________________________________\r\n");
		logger.info("Testing how many terminals can be created with the same ssh connection.");		
		conn = new Connection(host);
		conn.connect();
		boolean isAuthenticated = conn.authenticateWithPublicKey(username, keyfile, keyfilePass);
		assertTrue("Authentication failed.", isAuthenticated);
		Exception actualProblem = null;   

		while (true) {
			try {
				makeSession(conn);
				sessionCount++;
			} catch (Exception e) {
				actualProblem = e;
				break;
			}
		}

		assertTrue(actualProblem.getMessage().startsWith("Could not open channel (The server refused to open the channel"));
		assertTrue("Created " + sessionCount + " sessions but only expected 10 to work. ",  sessionCount < 11);
		assertTrue("Created " + sessionCount + " sessions. Should be able to create 10 with the same ssh connection.",  sessionCount > 9);
		System.out.println("_________________________________________________________________________________________");		
	}	

	private void makeSession(Connection conn) throws Exception {
		Session sess = conn.openSession();
		sess.requestPTY("vt100", 80, 24, 0, 0, null);
		sess.startShell();
		InputStream in = sess.getStdout();
		int len = in.read(buff);
		assertTrue("no output to terminal", len > -1);
		logger.info("session:\t\t" + (sessionCount + 1) + " " + new String(buff, 0, len).trim());
	}

	
}
