package monitor.implementation.shell;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import monitor.model.Configuration;

import org.junit.Ignore;
import org.junit.Test;

import ch.ethz.ssh2.ChannelCondition;
import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;

public class SSH2test {

	static String host = Configuration.getInstance().getDefaultHostForTests();
	String username = Configuration.getInstance().getUser(host).getName();
	File keyfile = new File("/home/" + username + "/.ssh/id_rsa"); 
	String keyfilePass = null; // will be ignored if not needed
	
	boolean useExampleMethod = false;
	
	@Test
	public void testTerminal() throws Exception {
		
		Connection conn = new Connection(host);
		conn.connect();
		boolean isAuthenticated = conn.authenticateWithPublicKey(username, keyfile, keyfilePass);
		assertTrue("Authentication failed.", isAuthenticated);
		
		Session sess = conn.openSession();
		
		sess.requestPTY("vt100", 80, 24, 0, 0, null);
		sess.startShell();

		Thread.sleep(500);		
		//String output = getTerminalOutput(sess);
		String output = readTerminalOutput(sess);
		System.out.print(output);
	
		assertTrue("expected Last login:", output.lastIndexOf("Last login:") > 0);

		String command = "echo $$\n";
		
		OutputStream out = sess.getStdin();

		out.write(command.getBytes());
		Thread.sleep(500);
		
		output = readTerminalOutput(sess);
		System.out.print(output);

		out.write("uname\n".getBytes());
		Thread.sleep(100);
		output = readTerminalOutput(sess);
		System.out.print(output);

		out.write("echo $$\n".getBytes());
		Thread.sleep(100);
		output = readTerminalOutput(sess);
		System.out.print(output);

		/*
		out.write("top\n".getBytes());
		Thread.sleep(5000);
		output = readTerminalOutput(sess);
		System.out.println(output);		
		*/
		
		conn.close();
	}
	
	/** Test two terminal sessions sharing the same ssh connection. */
	// TODO - need another test - this only gets the echoed text and not the output
	@Test @Ignore
	public void twoSessions() throws Exception {
		Connection conn = new Connection(host);
		conn.connect();
		boolean isAuthenticated = conn.authenticateWithPublicKey(username, keyfile, keyfilePass);
		assertTrue("Authentication failed.", isAuthenticated);

		// open the first terminal and check its output
		Session sess1 = conn.openSession();
		sess1.requestPTY("vt100", 80, 24, 0, 0, null);
		sess1.startShell();
		Thread.sleep(500);
		String output = readTerminalOutput(sess1);
		System.out.println("---------- output from session one ------");
		System.out.print(output);
		assertTrue("expected Last login:", output.lastIndexOf("Last login:") > -1);
		
		// open the second terminal
		Session sess2 = conn.openSession();
		sess2.requestPTY("vt100", 80, 24, 0, 0, null);
		sess2.startShell();
		Thread.sleep(500);
		String output21 = readTerminalOutput(sess2);
		System.out.println("---------- output from session two ------");
		System.out.print(output21);

		// write a command to the first terminal
		OutputStream out1 = sess1.getStdin();
		long sleepStart = System.currentTimeMillis();
		out1.write("sleep 1 ; echo $$ ; echo done\n".getBytes());

		// write a command to the second terminal while the first terminal command is still running
		OutputStream out2 = sess2.getStdin();
		out2.write("echo $$\n".getBytes());
		Thread.sleep(500);		
		output = readTerminalOutput(sess2);
		System.out.print(output);
		assertTrue("expected Last login:", output21.lastIndexOf("Last login:") > -1 || output.lastIndexOf("Last login:") > -1);
		assertTrue("expected echo $$:", output.lastIndexOf("echo $$") > -1);		
		
		if (System.currentTimeMillis() - sleepStart < 1000) {
			Thread.sleep(2000 - (System.currentTimeMillis() - sleepStart));
		}
		
		output = readTerminalOutput(sess1);
		System.out.println("\n---------- output from session one ------");
		System.out.print(output);
		assertTrue("expected 'done' but was: " + output, output.lastIndexOf("\r\ndone\r\n") > -1);
		
		sess1.close();
		sess2.close();
		conn.close();
	}
	
	private String readTerminalOutput(Session sess) throws Exception {
		if (useExampleMethod) {
			return getTerminalOutput(sess);
		}
		StringBuilder allOutput = new StringBuilder();
		String nextBit = "";
		int loops = 0;
		do {
			Thread.sleep(100);
			nextBit = readNextBitOfTerminalOutput(sess);
			allOutput.append(nextBit);
		} 
		while (nextBit.length() > 0 && loops++ < 20);
		return allOutput.toString();
	}

	private String readNextBitOfTerminalOutput(Session sess) throws Exception {
		InputStream in = sess.getStdout();
		byte[] buff = new byte[8192];

		if (in.available() == 0) {
			return "";
		}
		int len = in.read(buff);
		if (len == -1)
			return "";
		char[] chars = new char[len];
		for (int c = 0; c < len; c++) {
			chars[c] = (char) (buff[c] & 0xff);
		}
		return new String(chars);
	}
	
	
	/**  This was copied from SingleThreadStdoutStderr in ganymed-ssh2-build251beta1/examples 
	 *  but it doesn't seem to work with a psudo-terminal. */
	private String getTerminalOutput(Session sess) throws Exception {

		InputStream stdout = sess.getStdout();
		InputStream stderr = sess.getStderr();

		StringBuilder output = new StringBuilder();

		byte[] buffer = new byte[8192];

		while (true) {
			if ((stdout.available() == 0) && (stderr.available() == 0)) {
				/*
				 * Even though currently there is no data available, it may be
				 * that new data arrives and the session's underlying channel is
				 * closed before we call waitForCondition(). This means that EOF
				 * and STDOUT_DATA (or STDERR_DATA, or both) may be set
				 * together.
				 */

				int conditions = sess.waitForCondition(ChannelCondition.STDOUT_DATA | ChannelCondition.STDERR_DATA | ChannelCondition.EOF, 2000);

				/* Wait no longer than 2 seconds (= 2000 milliseconds) */

				if ((conditions & ChannelCondition.TIMEOUT) != 0) {
					/* A timeout occured. */
					throw new IOException("Timeout while waiting for data from peer.");
				}

				/*
				 * Here we do not need to check separately for CLOSED, since
				 * CLOSED implies EOF
				 */

				if ((conditions & ChannelCondition.EOF) != 0) {
					/* The remote side won't send us further data... */

					if ((conditions & (ChannelCondition.STDOUT_DATA | ChannelCondition.STDERR_DATA)) == 0) {
						/*
						 * ... and we have consumed all data in the local
						 * arrival window.
						 */
						break;
					}
				}

				/* OK, either STDOUT_DATA or STDERR_DATA (or both) is set. */

				// You can be paranoid and check that the library is not going
				// nuts:
				// if ((conditions & (ChannelCondition.STDOUT_DATA |
				// ChannelCondition.STDERR_DATA)) == 0)
				// throw new
				// IllegalStateException("Unexpected condition result (" +
				// conditions + ")");
			}

			/*
			 * If you below replace "while" with "if", then the way the output
			 * appears on the local stdout and stder streams is more "balanced".
			 * Addtionally reducing the buffer size will also improve the
			 * interleaving, but performance will slightly suffer. OKOK, that
			 * all matters only if you get HUGE amounts of stdout and stderr
			 * data =)
			 */

			while (stdout.available() > 0) {
				int len = stdout.read(buffer);
				if (len > 0) { // this check is somewhat paranoid
					char[] chars = new char[len];
					for (int c = 0; c < len; c++) {
						chars[c] = (char) buffer[c];
					}
					output.append(chars);
				}
			}

			while (stderr.available() > 0) {
				int len = stderr.read(buffer);
				if (len > 0) { // this check is somewhat paranoid
					char[] chars = new char[len];
					for (int c = 0; c < len; c++) {
						chars[c] = (char) buffer[c];
					}
					output.append(chars);
				}
			}
		}
		return output.toString();
	}
	
}
