package monitor.implementation.shell;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import monitor.implementation.MonitorNoStackTraceRuntimeException;
import monitor.implementation.MonitorRuntimeException;
import monitor.implementation.session.NextSessionId;
import monitor.model.Command;
import monitor.model.CommandResult;
import monitor.model.CommandStatus;
import monitor.model.Configuration;
import monitor.model.Server;

/**
 * I execute commands. I throw exceptions if anything goes wrong. 
 * I am not thread safe. If a command does not complete a new thread is started.
 * @deprecated with 1.6_20 waitForProcessExit in lava.lang.UNIXProcess uses too much cpu.
 * Use SSHExecuter instead.
 */
public class BashExecuter implements CommandExecuter {
	
	static Logger logger = Logger.getLogger(BashExecuter.class.getName());
	private static final boolean logFine = Configuration.getInstance().isLogFine();	

	private Server server;
	private Process shell;
	private boolean shellProcessStarted = false;
	private String program;
	private String args;
	private int bashProcessId;

	private OutputStream outputToShell;
	
	private String echoText = " ";
	private int commandCounter = 0;
	
	private InputFromBashReader inputFromShellReader;
	private Thread inputFromShellReaderThread;
	private Exception inputFromShellReaderException;	
	private CommandStatus lastCommandStatus;	
	private int lastChunkRead = -1;
	private boolean wantToBeInterupted = false;
	private String sessionId = null;
	private boolean showSSHcopyIdHintInException = false;
	private boolean addLoggedOnMessageToChunkedOutput = false;

	private boolean alreadyKilled;	

	/**
	 * Creates a local bash process.	private SessionIdMaker nextSessionId = new NextSessionId();
	 */
	public BashExecuter() {
		sessionId = new NextSessionId().makeNewSessionId();
		createProcess(new Server("localhost"), "bash", "-s");
	}

	/**
	 * Starts some program other than bash in a separate process.
	 * <p>e.g.<p>
	 * new BashExecuter(server, "ssh", "-v", user.getName() + '@' +
	 * server.getHost());
	 */
	public BashExecuter(Server server, String sessionId, String...programAndArguments) {
		this.sessionId = sessionId;	
		showSSHcopyIdHintInException = true;
		addLoggedOnMessageToChunkedOutput = true;		
		createProcess(server, programAndArguments);
	}

	/**
	 */
	private void createProcess(Server server, String...programAndArguments) {
		this.server = server;
		ProcessBuilder processBuilder = new ProcessBuilder(programAndArguments);
		processBuilder.redirectErrorStream(true);
		try {
			program = programAndArguments[0];
			StringBuilder sb = new StringBuilder();
			for (int x =1; x < programAndArguments.length; x++) {
				sb.append(programAndArguments[x]);
				if (x < programAndArguments.length -1) {
					sb.append(' ');
				}
			}
			args = sb.toString();
			if (logFine) logger.log(Level.INFO, String.format("%s %s", program, args));
			shell = processBuilder.start();
			new ProcessReaperTweaker().reduceThreadPriority(); // makes no difference
			shellProcessStarted = true;
		} catch (IOException e) {
			throw new MonitorRuntimeException(e);
		}
		InputStream inputFromShell = shell.getInputStream();
		outputToShell = shell.getOutputStream();
		createAndStartInputFromShellReader(inputFromShell);
		try {
			testTerminal();
		} catch (Exception e) {
			
			throw new MonitorRuntimeException(e);
		}
	}

	@Override
	public void testTerminal() throws Exception {
		String loggedOnMessage = executeTestCommand(showSSHcopyIdHintInException, Configuration.getInstance().getSessionLoginTimeoutSeconds() * 1000);
		if (logFine) logger.log(Level.INFO, loggedOnMessage);
	}	

	/** check that the session is working and set the bash process id. This also works for the bash program. */
	private String executeTestCommand(boolean showHintInException, int millisBeforeTimeout) {
		String response;
		Command getBashProcessId = new Command("echo $$", true); // should return the bash process id
		getBashProcessId.setExpectOutput(true);
		getBashProcessId.setMillisBeforeTimeout(millisBeforeTimeout);
		response = executeCommand(getBashProcessId).getOutput().trim(); // the response ends with a /n
		response = response.substring(response.lastIndexOf('\n') + 1); // there should be a newline before the PID
		if (!(response.length() > 0 && Pattern.matches("\\d+", response))) { // PID should be a number
			String outputFromOpenSSH = getChunkedOutput().getAllChunks();
			if (response.endsWith("Name or service not known")) {
				throw new MonitorNoStackTraceRuntimeException(outputFromOpenSSH);
			}
			String hint = !showHintInException ? "" :
				"\nHint: If you don't see 'Authentication succeeded (publickey)' above try\n      ssh-keygen -t rsa                            # creates my public and private rsa keys" +
				"\n      ssh-copy-id -i ~/.ssh/id_rsa.pub username@mystery          # copys my public key to the host" +
				"\nAlso worth trying /etc/init.d/ssh restart";				
			throw new MonitorRuntimeException(String.format("Test command 'echo $$' on %s returned '%s'. Expected a process id. ssh output: \n\"%s\"" +
				hint, server.getHost(), response, outputFromOpenSSH));
		}
		setBashProcessId(Integer.valueOf(response));
		clearChunkedOutput();	
		String loggedOnMessage = makeLoggedOnMessage();
		if (addLoggedOnMessageToChunkedOutput) {
			appendToChunkedOutput(loggedOnMessage);	
		}
		return loggedOnMessage;
	}
	
	private String makeLoggedOnMessage() {
		return String.format("logged on to %s PID %d sessionId %s\n", server.getHost(), getBashProcessId(), getSessionId());
	}
	
	@Override
	public void createAndStartInputFromShellReader(InputStream inputFromShell) {
		inputFromShellReader = new InputFromBashReader(inputFromShell, this, getSessionId());
		inputFromShellReaderThread = new Thread(inputFromShellReader, server.getHost() + "-PID:?");
		inputFromShellReaderThread.start();
	}
	
	/* (non-Javadoc)
	 * @see monitor.implementation.shell.CommandExecuter#killProcess()
	 */
	@Override
	public void killProcess() {
		if (alreadyKilled)
			return;
		String message = "";
		if (shellProcessStarted) {
			try {						
				shell.destroy();
				if (logFine) logger.log(Level.INFO, "bash process " + bashProcessId + " killed.");
			} catch (Exception e) {
				message = e.getMessage();
			}
			shellProcessStarted = false;
		}		
		try {
			stopReaderThread();
		} catch (Exception e) {
			message = message + e.getMessage();
		}
		alreadyKilled = true;
		if (message.length() > 0) {
			throw new MonitorRuntimeException(message);
		}
	}
	
	@Override
	public void stopReaderThread() throws InterruptedException {
		CountDownLatch stoppedSignal = new CountDownLatch(1);
		inputFromShellReader.stopRunning(stoppedSignal);
		inputFromShellReader.addCommandToQueue(commandCounter++, new Command(CommandAndNumber.WAKE_UP));
		boolean stopped = stoppedSignal.await(1000, TimeUnit.MILLISECONDS);
		if (!stopped) {
			inputFromShellReaderThread.interrupt();
			throw new MonitorRuntimeException("Thread " + inputFromShellReaderThread.getName() + " did not stop when asked.");
		}
	}	
	
	
	
	/* (non-Javadoc)
	 * @see monitor.implementation.shell.CommandExecuter#resetChunkedOutput()
	 */
	@Override
	public synchronized void resetChunkedOutput(String newSessionId, int millisBeforeInterupting) throws Exception {
		this.sessionId = newSessionId;
		inputFromShellReader.changeSessionId(newSessionId);
		CountDownLatch stopFinishedCheckingSignal = new CountDownLatch(1);		
		inputFromShellReader.stopFinishedChecking(stopFinishedCheckingSignal);
		inputFromShellReader.addCommandToQueue(commandCounter++, new Command("# wake up !"));
		boolean stopped = stopFinishedCheckingSignal.await(millisBeforeInterupting, TimeUnit.MILLISECONDS);
		if (!stopped) {
			inputFromShellReaderThread.interrupt();
			throw new MonitorRuntimeException("Thread " + inputFromShellReaderThread.getName() + " did not stop reading input when asked.");
		}
		flushInputFromShell();
		inputFromShellReader.clearChunkedOutput();
		lastChunkRead = -1;
	}	

	/* (non-Javadoc)
	 * @see monitor.implementation.shell.CommandExecuter#flushInputFromShell()
	 */
	@Override	
	public void flushInputFromShell() {
		InputStream inputFromShell = shell.getInputStream();
		int available;
		try {
			outputToShell.write("echo $$\n".getBytes());
			outputToShell.flush();		
			available = inputFromShell.available();
			while (available > 0) {
				inputFromShell.read(new byte[available]);
				available = inputFromShell.available();
			}
		} catch (IOException e) {
			throw new MonitorRuntimeException(e);
		}
	}
	

	@Override	
	public void stopReadingInput() {
		inputFromShellReader.stopFinishedChecking(new CountDownLatch(1));
	}
	
	@Override
	public synchronized void clearChunkedOutput() {
		inputFromShellReader.clearChunkedOutput();
		lastChunkRead = -1;
	}

	@Override
	public void writeToShell(String text) {
		try {
			outputToShell.write(text.getBytes());
			outputToShell.flush();			
		} catch (IOException e) {
			logger.log(Level.SEVERE, String.format("problem writing text to shell on %s", server), e);
			throw new MonitorRuntimeException(e);
		}
	}
	
	/* (non-Javadoc)
	 * @see monitor.implementation.shell.CommandExecuter#executeCommand(monitor.model.Command)
	 */
	@Override	
	public CommandResult executeCommand(Command command) {
		logger.fine(command.getRequest());
		String cmd = command.getRequest().concat("\n");
		String response = "";
		// write the command
		try {
			echo(command);
			// write this command to the shells input
			outputToShell.write(cmd.getBytes());
			outputToShell.flush();
			if ((command.getRequest().equals("exit") || command.getRequest().equals("logout"))) {
				lastCommandStatus = CommandStatus.FINISHED;
				killProcess();
			} else {
				// echo some unlikely text so we know when it has finished
				echoText = inputFromShellReader.formatEchoText(commandCounter);
				outputToShell.write("echo ".concat(echoText).getBytes());
				outputToShell.flush();
				lastCommandStatus = CommandStatus.RUNNING;
				try {
					wantToBeInterupted = true;
					inputFromShellReader.setBashExecuterThread(Thread.currentThread());
					// wake up the thread that reads output
					inputFromShellReader.addCommandToQueue(commandCounter++, command);
					Thread.sleep(command.getMillisBeforeTimeout());
					wantToBeInterupted = false;
				} catch (InterruptedException ie) {
					// good, we were interrupted by InputFromShellReader thread which found output before we gave up waiting
					if (logFine) logger.info("Interupted while waiting because got output from command: " + cmd);
				}
				// but - InputFromShellReader now has our thread and could interrupt anytime so wantsToBeInterupted = false
				wantToBeInterupted = false;
			}
			int chunkAfter = getChunkedOutput().getHighestChunkNumber();
			response = getChunkedOutput().getChunks(lastChunkRead + 1, chunkAfter);
			lastChunkRead = chunkAfter;			
		} catch (Exception e) {
			if (!command.getRequest().equals("exit") && !command.getRequest().equals("logout")) {			
				logger.log(Level.SEVERE, String.format("problem executing command: %s on %s", command.getRequest(), server), e);
			}
			throw new MonitorRuntimeException(e);
		}
		if (inputFromShellReaderException != null) {
			throw new MonitorRuntimeException(inputFromShellReaderException);
		}
		CommandResult commandResult = new CommandResult();
		commandResult.setCommandStatus(lastCommandStatus);
		commandResult.setSessionId(getSessionId());
		commandResult.setOutput(response);
		commandResult.setChunkNumber(lastChunkRead);
		return commandResult;
	}
	
	private void echo(Command command) {
		if (command.isLocalEcho()) {
			getChunkedOutput().append("> ".concat(command.getRequest().concat("\n")));
		}
	}

	/* (non-Javadoc)
	 * @see monitor.implementation.shell.CommandExecuter#getLastCommandStatus()
	 */
	@Override	
	public CommandStatus getLastCommandStatus() {
		return lastCommandStatus;
	}

	/* (non-Javadoc)
	 * @see monitor.implementation.shell.CommandExecuter#getChunkedOutput()
	 */
	@Override	
	public ChunkedOutput getChunkedOutput() {
		return inputFromShellReader.getChunkedOutput();
	}

	/* (non-Javadoc)
	 * @see monitor.implementation.shell.CommandExecuter#appendToChunkedOutput(java.lang.String)
	 */
	@Override	
	public synchronized void appendToChunkedOutput(String chunk) {
		if (getChunkedOutput().getHighestChunkNumber() > lastChunkRead ) {
			// can't stop the chunk from being included after some command output that has not been processed yet
		} else {
			// wont duplicate the chunk in the next commands output
			lastChunkRead++;   
		}
		inputFromShellReader.getChunkedOutput().append(chunk);
	}	
	
	/* (non-Javadoc)
	 * @see monitor.implementation.shell.CommandExecuter#getSessionId()
	 */
	@Override	
	public String getSessionId() {
		return sessionId;
	}
	
	/* (non-Javadoc)
	 * @see monitor.implementation.shell.CommandExecuter#saveInputFromShellReaderException(java.lang.Exception)
	 */
	@Override	
	public void saveInputFromShellReaderException(Exception e) {
		inputFromShellReaderException = e;
	}

	/* (non-Javadoc)
	 * @see monitor.implementation.shell.CommandExecuter#setLastCommandStatus(monitor.model.CommandStatus)
	 */
	@Override	
	public void setLastCommandStatus(CommandStatus status) {
		lastCommandStatus = status;
	}
	
	/* (non-Javadoc)
	 * @see monitor.implementation.shell.CommandExecuter#getRunningCommand()
	 */
	@Override	
	public String getRunningCommand() {
		return inputFromShellReader.getRunningCommand();
	}

	/* (non-Javadoc)
	 * @see monitor.implementation.shell.CommandExecuter#isInteruptable()
	 */
	@Override	
	public boolean isInteruptable() {
		return wantToBeInterupted;
	}

	/* (non-Javadoc)
	 * @see monitor.implementation.shell.CommandExecuter#sendOutputTo(monitor.implementation.shell.ChunkedOutput)
	 */
	@Override	
	public void copyOutputTo(ChunkedOutput chunkedOutput) {
		inputFromShellReader.sendOutputTo(chunkedOutput);
	}

	/* (non-Javadoc)
	 * @see monitor.implementation.shell.CommandExecuter#getBashProcessId()
	 */
	@Override	
	public int getBashProcessId() {
		return bashProcessId;
	}
	
	/* (non-Javadoc)
	 * @see monitor.implementation.shell.CommandExecuter#setBashProcessId(int)
	 */
	@Override	
	public void setBashProcessId(int bashProcessId) {
		this.bashProcessId = bashProcessId;
		inputFromShellReaderThread.setName(server.getHost() + "-PID:" + bashProcessId);
	}

	@Override
	public String getLoggedOnUserName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLoggedOnUserPassword() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getHostName() {
		return server.getHost();
	}
	
}