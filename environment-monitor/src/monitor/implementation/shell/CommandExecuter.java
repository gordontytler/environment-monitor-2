package monitor.implementation.shell;

import java.io.InputStream;

import monitor.implementation.MonitorRuntimeException;
import monitor.model.Command;
import monitor.model.CommandResult;
import monitor.model.CommandStatus;

public interface CommandExecuter {

	/** 
	 * Reset the input from shell reader thread so that the session can be reused. 
	 * @param newSessionId a new sessionId prevents the old client from using it
	 * @param millisBeforeInterupting how long should the stopFinishedCheckingSignal wait for before interrupting the InputFromShellReader thread
	 * @throws Exception if the reader could not be stopped 
	 * @throws Exception */
	public void resetChunkedOutput(String newSessionId, int millisBeforeInterupting) throws Exception;
	/** 
	 * Even though the last command may have finished there could be more output on the way.
	 * Child processes may still be running or may produce final output when killed. We flush the output when closing the session 
	 * and also when opening it.
	 * <p>e.g.<p><code>
	monitor.implementation.MonitorRuntimeException: Test command 'echo $$' on gordon-netbook returned '      8412 Terminated              | cat'. Expected a process id. ssh output: 
	<br>"logged on to gordon-netbook session 1 PID 8324 sessionId 7
	<br>8324
	<br>-bash: line 6:  8409 Done                    echo blah1
	<br>      8410 Terminated              | sleep 10
	<br>     8411 Terminated              | cat
	<br>      8412 Terminated              | cat
	<br>"
	<br>	at monitor.implementation.session.Session.executeTestCommand(Session.java:75)
		</code>
	 * @throws Exception 
	 */
	public void flushInputFromShell() throws Exception;
	public void clearChunkedOutput();

	/**
	 * @throws MonitorRuntimeException  
	 *   
	 */
	public CommandResult executeCommand(Command command);

	/**
	 * This is the status of the last command When the process reads a series of commands from stdin.
	 */
	public CommandStatus getLastCommandStatus();
	public ChunkedOutput getChunkedOutput();
	public void appendToChunkedOutput(String chunk);
	public String getSessionId();
	public void saveInputFromShellReaderException(Exception e);
	public void setLastCommandStatus(CommandStatus status);
	public String getRunningCommand();
	public boolean isInteruptable();
	public void killProcess();
	/** write the output to the same stream as another master process. It will be interleaved. */
	public void copyOutputTo(ChunkedOutput chunkedOutput);
	public int getBashProcessId();
	public void setBashProcessId(int bashProcessId);
	/** This is called before killing the currently running command and its sub-processes. 
	 * There is no need to check that the inputFromSSHReader is not blocked on a read because
	 * killing the process will cause bash to send some terminated message which should unblock the reader. */
	public void stopReadingInput();
	public void testTerminal() throws Exception;
	public void createAndStartInputFromShellReader(InputStream inputFromShell);
	public void stopReaderThread() throws InterruptedException;
	/** Use this when the program expects some interactive input such as a password. */
	public void writeToShell(String text);
	public String getLoggedOnUserName();
	public String getLoggedOnUserPassword();
	public String getHostName();

}