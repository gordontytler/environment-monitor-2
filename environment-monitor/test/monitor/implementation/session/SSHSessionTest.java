package monitor.implementation.session;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.logging.Logger;

import monitor.model.Command;
import monitor.model.CommandResult;
import monitor.model.CommandStatus;
import monitor.model.Configuration;
import monitor.model.Server;

import org.junit.After;
import org.junit.Test;


public class SSHSessionTest {
	
	static Logger logger = Logger.getLogger(SSHSessionTest.class.getName());

	static String host = Configuration.getInstance().getDefaultHostForTests();
	static Server server = new Server(host);
	static SSHConnection sshConnection;
	static PseudoTerminal terminal;	
	
	private AllSessionPools allSessionPools = AllSessionPools.getInstance();	
	
	@After
	public void tearDown() throws Exception {
		if (sshConnection != null)
			sshConnection.destroy();
	}
	

	@Test
	public void testConstructor() throws Exception {
		long startTime = System.currentTimeMillis();
		sshConnection = new SSHConnection(new Server(host), null, 0);
		terminal = sshConnection.createPseudoTerminal();
		new Session(new Server(host), terminal, "SessionTest", 0);
		long timeTaken = System.currentTimeMillis() - startTime;
		assertTrue("took " + timeTaken + "ms", timeTaken < 7000);
	}	
	
	
	
	@Test
	public void testKillRunningCommand() throws Exception {
		Session session = allSessionPools.getServerSessionPool(server).getSession("SSHSessionTest.testKillRunningCommand");
		Command command = new Command("sleep 10");
		CommandResult commandResult = session.executeCommand(command);
		logger.info("sleep 10 command is running");
		assertEquals(CommandStatus.RUNNING, commandResult.getCommandStatus());
		assertEquals(CommandStatus.RUNNING, session.getLastCommandStatus());
		logger.info("about to kill the sleep 10 command");
		CommandResult killResult = session.killRunningCommand("SessionTest");
		assertEquals(CommandStatus.FINISHED, killResult.getCommandStatus());
		logger.info("CommandResult from session.killRunningCommand was FINISHED");
		assertEquals(CommandStatus.FINISHED, session.getLastCommandStatus());
		assertEquals("Terminated\r\n",  killResult.getOutput());		
	}
	
}
