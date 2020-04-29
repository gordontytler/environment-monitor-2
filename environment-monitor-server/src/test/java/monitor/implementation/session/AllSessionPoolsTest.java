package monitor.implementation.session;

import static org.junit.Assert.assertEquals;
import monitor.model.Command;
import monitor.model.CommandResult;
import monitor.model.CommandStatus;
import monitor.model.Configuration;
import monitor.model.Server;

import org.junit.Test;

public class AllSessionPoolsTest {

	static String host = Configuration.getInstance().getDefaultHostForTests();
	static Server server = new Server(host);
	
	private AllSessionPools allSessionPools = AllSessionPools.getInstance();	
	
	
	@Test
	/**
	 * Check that logout works when the command is still running.
	 */

	public void testLogoutAllSessionsOnAllServers() {
		Session session = allSessionPools.getServerSessionPool(server).getSession("SessionTest");
		Command command = new Command("sleep 20");
		command.setMillisBeforeTimeout(1);
		CommandResult commandResult = session.executeCommand(command);
		assertEquals(CommandStatus.RUNNING, commandResult.getCommandStatus());
		assertEquals(CommandStatus.RUNNING, session.getLastCommandStatus());
		allSessionPools.logoutAllSessionsOnAllServers("testLogoutAllSessionsOnAllServers");
		if (allSessionPools.getAllSessions().size() > 0) {
			allSessionPools.dumpAllSessionsOnAllServers("to show which ones remain after logoutAllSessionsOnAllServers");
			assertEquals("expected no sessions after logoutAllSessionsOnAllServers", 0, allSessionPools.getAllSessions().size());
		}
	}
		
	
	
}
