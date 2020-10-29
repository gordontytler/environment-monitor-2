package monitor.implementation.session;

import monitor.implementation.shell.SSHExecuter;
import monitor.model.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class ProcessKillerChildProcessTest {

	static String host = Configuration.getInstance().getDefaultHostForTests();
	static Server server = new Server(host);
	
	private AllSessionPools allSessionPools = AllSessionPools.getInstance();
	
	Session session;
	Session controlSession;		
	

	@Before
	public void setUp() {
		allSessionPools.getServerSessionPool(server).destroySSHConnections();
		session = allSessionPools.getServerSessionPool(server).getSession(this.getClass().getSimpleName());
		controlSession = allSessionPools.getServerSessionPool(server).getSession(this.getClass().getSimpleName());
		SSHExecuter sshExecuter = (SSHExecuter) session.getCommandExecuter();
		sshExecuter.getInputFromSSHReader().setLogFine(true);
		sshExecuter.getInputFromSSHReader().setLogFineFilter(true);
		System.out.println("FINISHED setup()");
	}
	
	@After
	public void tearDown() throws Exception {
		System.out.println("tearDown()");
		if (session != null) {
			new ProcessKiller().killSubProcesses(server, session.getBashProcessId(), "ProcessKillerTest.tearDown");
		}
		allSessionPools.closeAllFinishedSessionsOnAllServers();
		SSHExecuter sshExecuter = (SSHExecuter) session.getCommandExecuter();
		sshExecuter.getInputFromSSHReader().setLogFine(true);
		sshExecuter.getInputFromSSHReader().setLogFineFilter(true);
	}

	/** 
	 * The parent has one child and the child has a child.
	 * 
	 * 	ls /var/log/*syslog* -ltr  | tail -1 | awk '{print $8}'  | xargs tail -F
	 *	
	 *	tytlerg   9060  1928  0 Jun22 pts/1    00:00:00 bash
	 *	tytlerg  10302  9060  0 16:12 pts/1    00:00:00 xargs tail -F
	 *	tytlerg  10303 10302  0 16:12 pts/1    00:00:00 tail -F /var/log/syslog
 	 *
 	 * If this test crashes a few times you might need to 
 	 * 
 	 * ps -ef | grep 'tail -F /var/log/syslo[g]' | awk '{print $2}' | xargs kill
 	 * 
 	 * because the allSessionPools.closeAllSessionsOnAllServers(); does not kill the subprocesses.
 	 *
	 */
	@Test
	public void getChildOfChildAndKillThem() {
		System.out.println("\n\nstart getChildOfChildAndKillThem()\n\n");
		Command command = new Command("sudo ls /var/log/*syslog* -ltr  | tail -1 | awk '{print $NF}'  | xargs tail -F");
		CommandResult commandResult = session.executeCommand(command);
		System.out.println("output from tail: " + commandResult.getOutput());
		assertEquals(CommandStatus.RUNNING, commandResult.getCommandStatus());
		ProcessKiller processKiller = new ProcessKiller();
		ArrayList<Integer> childProcesses = processKiller.getChildren(session.getBashProcessId(), controlSession);
		assertEquals(4, childProcesses.size());
		CommandResult checkResult = controlSession.executeCommand(new Command(String.format("ps -p %d -o comm | grep -v COMMAND", childProcesses.get(0)), false));
		assertEquals("xargs", checkResult.getOutput().trim());
		checkResult = controlSession.executeCommand(new Command(String.format("ps -p %d -o comm | grep -v COMMAND", childProcesses.get(1)), false));
		assertEquals("awk", checkResult.getOutput().trim());
		checkResult = controlSession.executeCommand(new Command(String.format("ps -p %d -o comm | grep -v COMMAND", childProcesses.get(2)), false));
		assertEquals("tail", checkResult.getOutput().trim());
		checkResult = controlSession.executeCommand(new Command(String.format("ps -p %d -o comm | grep -v COMMAND", childProcesses.get(3)), false));
		assertEquals("sudo", checkResult.getOutput().trim());


		// now kill them
		processKiller.killSubProcesses(server, session.getBashProcessId(), "ProcessKillerTest");
		childProcesses = processKiller.getChildren(session.getBashProcessId(), controlSession);
		assertEquals(0, childProcesses.size());
		System.out.println("\n\nend getChildOfChildAndKillThem()\n\n");
	}

}
