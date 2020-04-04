package monitor.implementation.shell;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import monitor.implementation.session.AllSessionPools;
import monitor.implementation.session.NextSessionId;
import monitor.implementation.session.PseudoTerminal;
import monitor.implementation.session.SSHConnection;
import monitor.implementation.session.Session;
import monitor.model.Command;
import monitor.model.CommandResult;
import monitor.model.CommandStatus;
import monitor.model.Configuration;
import monitor.model.Server;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class InputFromSSHReaderTest {

	static String host = Configuration.getInstance().getDefaultHostForTests();
	static String userName = Configuration.getInstance().getUser(host).getName(); 
	static SSHConnection sshConnection;
	static PseudoTerminal terminal;	
	static SSHExecuter executer;

	/** Construct an SSHExecuter using a {@link PseudoTerminal} and after it has run its tests check how long it took and check the prompt. */
	@BeforeClass
	public static void setUp() throws Exception {
		Configuration.getInstance().setLogFine(true);
		Configuration.getInstance().setLogFineSomething("InputFromSSHReader.filter", true);		
		
		sshConnection = new SSHConnection(new Server(host), null, 0);
		terminal = sshConnection.createPseudoTerminal();
		long startTime = System.currentTimeMillis();
		executer = new SSHExecuter(new Server(host), terminal, 200);
		long timeTaken = System.currentTimeMillis() - startTime;
		assertTrue("took " + timeTaken + "ms", timeTaken < 5000);
		// The prompt for auto user will depend on how the user is set up
		if ("auto".equals(sshConnection.getLoggedOnUserName())) {
			assertEquals("auto@" + host + ":/$ ", executer.getPrompt());
		} else {
			assertEquals(userName + "@" + host + ":~$ ", executer.getPrompt());	
		}
		
		executer.flushInputFromShell();		
	}

	@AfterClass
	public static void tearDown() {
		Configuration.getInstance().setLogFine(false);
		Configuration.getInstance().setLogFineSomething("InputFromSSHReader.filter", false);		
		terminal.destroy();
		sshConnection.destroy();
	}
	
	@Test
	public void testFilterEchoedCommand() throws Exception {
		InputFromSSHReader inputFromSSHReader = new InputFromSSHReader(null, executer, new NextSessionId().makeNewSessionId());
		String request = "this is the command request";
		Command command = new Command(request, true);
		String output = request + "\r\n" +
			"some output\r\n" +
			"some more output\r\n" +
			executer.getPrompt();
		byte[] buff = output.getBytes();
		String actual = inputFromSSHReader.filter(buff.length, buff, command);
		assertEquals("some output\r\nsome more output\r\n", actual);
		assertTrue(inputFromSSHReader.isFoundPrompt());
		
		// now don't remove the echoed command but still remove the prompt
		command = new Command(request);
		actual = inputFromSSHReader.filter(buff.length, buff, command);
		assertEquals(request + "\r\nsome output\r\nsome more output\r\n", actual);
		assertTrue(inputFromSSHReader.isFoundPrompt());
	}

	/*
	
	In the output below, it loops round twice. The first time it correctly removes the echoed command and correctly does not set foundPrompt.
	There is no output from the ps command so the next bit of output is just the prompt.
	 
	28-Dec-2010 13:12:02 monitor.implementation.shell.InputFromSSHReader filter
	INFO: unfiltered: ps --ppid 7735 -o pid | grep -v PID

	28-Dec-2010 13:12:02 monitor.implementation.shell.InputFromSSHReader filter
	INFO: foundPrompt false offset 37 lastNewline 36 len 37 :  
	28-Dec-2010 13:12:02 monitor.implementation.shell.InputFromSSHReader filter
	INFO: unfiltered: gordon@gordon-netbook:~$ 
	28-Dec-2010 13:12:02 monitor.implementation.shell.InputFromSSHReader filter
	INFO: foundPrompt false offset 0 lastNewline -1 len 25 : gordon@gordon-netbook:~$

	*/
	@Test
	public void testFilterJustPrompt() throws Exception {
		InputFromSSHReader inputFromSSHReader = new InputFromSSHReader(null, executer, null);
		Command command = new Command("a command producing no output", false);
		byte[] buff = executer.getPrompt().getBytes();
		String actual = inputFromSSHReader.filter(buff.length, buff, command);
		assertEquals("", actual);
		assertTrue(inputFromSSHReader.isFoundPrompt());
	}
	
	/*
	
	Here the output starts with the prompt from the previous command.
	
	INFO: unfiltered: gordon@gordon-netbook:~$ echo $$
	8795
	gordon@gordon-netbook:~$ 
	28-Dec-2010 14:01:31 monitor.implementation.shell.InputFromSSHReader filter
	INFO: foundPrompt false offset 0 lastNewline 39 len 65 : gordon@gordon-netbook:~$ echo $$
	8795
	gordon@gordon-netbook:~$  

	when trying to reproduce this error I got ...
	
	INFO: unfiltered: gordon@gordon-netbook:~$ echo $$
	8795
	gordon@gordon-netbook:~$ 
	28-Dec-2010 14:27:06 monitor.implementation.shell.InputFromSSHReader filter
	INFO: foundPrompt true offset 0 lastNewline 39 len 65 : gordon@gordon-netbook:~$ echo $$
	8795	
	
	
	
	*/
	@Test 	
	public void testMultiplePrompts() throws Exception {
		InputFromSSHReader inputFromSSHReader = new InputFromSSHReader(null, executer, null);
		String request = "echo $$";
		Command command = new Command(request, true); // Note: Using stty -echo makes the test and the strip echoed command option obsolete.   
		
		String output = executer.getPrompt() + request + "\r\n" +
		"8795\r\n" +
		executer.getPrompt();		

		byte[] buff = output.getBytes();
		String actual = inputFromSSHReader.filter(buff.length, buff, command);
		assertEquals("8795\r\n", actual);
		assertTrue(inputFromSSHReader.isFoundPrompt());
	}
	

	@Test
	public void testAutoPassword() {
		Session session = AllSessionPools.getInstance().getServerSessionPool(new Server(host)).getSession("testAutoPassword");
		
		Command whoami = new Command("whoami", false);
		CommandResult commandResult = session.executeCommand(whoami);		
		//CommandResult commandResult = executer.executeCommand(whoami);  // TODO why does this fail but using session works ?
		assertEquals(CommandStatus.FINISHED, commandResult.getCommandStatus());
		if ("auto".equals(sshConnection.getLoggedOnUserName())) {
			assertEquals("auto", commandResult.getOutput().trim());
		} else {
			assertEquals(userName.trim(), commandResult.getOutput().trim());
		}
		
		Command needsPassword = new Command("sudo -k whoami", false);
		commandResult = session.executeCommand(needsPassword);
		assertEquals(CommandStatus.FINISHED, commandResult.getCommandStatus());
		assertEquals("root", commandResult.getOutput().trim());
	}
	
}
