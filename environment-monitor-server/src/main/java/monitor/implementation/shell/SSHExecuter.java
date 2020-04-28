package monitor.implementation.shell;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import monitor.api.StackTraceFormatter;
import monitor.implementation.MonitorRuntimeException;
import monitor.implementation.session.PseudoTerminal;
import monitor.model.Command;
import monitor.model.CommandResult;
import monitor.model.CommandStatus;
import monitor.model.Configuration;
import monitor.model.Server;

public class SSHExecuter implements CommandExecuter {

	static Logger logger = Logger.getLogger(SSHExecuter.class.getName());
	static final boolean logFine = Configuration.getInstance().isLogFine();
	int defaultTestCommandTimeoutMillis = Configuration.getInstance().getTestCommandTimeoutMillis();
	int testCommandTimeoutMillis;
	
	private Server server;
	PseudoTerminal terminal;	
	private InputStream in;
	private OutputStream out;
	private String prompt;
	private int bashProcessId;
	
	private byte[] buff = new byte[8192];  // 2 ^ 13 or 2000 hex. Was ch.ethz.ssh2 was probably copied from some C source ?
	private InputFromSSHReader inputFromSSHReader;
	private Thread inputFromSSHReaderThread;	

	private String sessionId = null;
	private int lastChunkRead = -1;
	private Exception inputFromSSHReaderException;
	private CommandStatus lastCommandStatus;
	private boolean wantToBeInterupted;
	private int commandCounter = 0;
	private boolean alreadyKilled;
	private CountDownLatch exceptionSignal = new CountDownLatch(1);
	
	
	public SSHExecuter(Server server, PseudoTerminal terminal, int testCommandTimeoutMillis) {
		if (testCommandTimeoutMillis > 0) {
			this.testCommandTimeoutMillis = testCommandTimeoutMillis;
		} else {
			this.testCommandTimeoutMillis = defaultTestCommandTimeoutMillis;
		}
		this.server = server;
		this.terminal = terminal;
		this.sessionId = terminal.getSessionId();
		in = terminal.getStdout();
		out = terminal.getStdin();
		try {
			testTerminal();
			out.write("stty -echo; unalias ls\n".getBytes()); // turn off echo of input and colour for ls
			Thread.sleep(testCommandTimeoutMillis);		
			readTerminalOutput(in);
			Thread.sleep(testCommandTimeoutMillis);		
			readTerminalOutput(in);			
		} catch (Exception e) {
			throw new MonitorRuntimeException(e);			
		}
		createAndStartInputFromShellReader(in);
		if (logFine) logger.info(String.format("sessionId:%s pseudu-terminal started on %s with bash process id %d and prompt %s", sessionId, server, bashProcessId, prompt));
	}
	
	/** Check that the terminal is working and determine the prompt and the bash process id. */
	public void testTerminal() throws Exception {
		Thread.sleep(testCommandTimeoutMillis);
		PromptAndProcessIdExtractor extractor = new PromptAndProcessIdExtractor();		
		readTerminalOutput(in); // flush out the welcome message or previous output
		out.write("echo $$\n".getBytes());
		String firstOutput = "";
		int attempts = 0;
		// wait till we have a reasonable amount of data
		while (firstOutput.length() < 5 && attempts++ < 10) {
			Thread.sleep(testCommandTimeoutMillis);
			firstOutput = firstOutput + readTerminalOutput(in);	
		}
		// when command is echoed expect something like this:  "echo $$\r\nuser@host:~$ echo $$\r\n87401\r\nuser@host:~$ " 
		int lastNewline = firstOutput.lastIndexOf('\n');
		attempts = 0;
		while (lastNewline == -1 && attempts++ < 10) {
			Thread.sleep(testCommandTimeoutMillis);
			firstOutput = firstOutput + readTerminalOutput(in);
			lastNewline = firstOutput.lastIndexOf('\n');
		}
		if (lastNewline == -1) {
			throw new MonitorRuntimeException(String.format("Expected a newline in reply from %s. Test command 'echo $$' returned: %s", server.getHost(), firstOutput));
		}
		String extractorMessage = extractor.extractPromptAndProcessId(firstOutput, lastNewline); 
		if (!"OK".equals(extractorMessage)) {
			/* 
			  The output might be like this but stop after the second echo $$\r\n or after the process number  
					Last login: Tue Dec 28 22:24:35 2010 from gordon-netbook
					echo $$
					gordon@gordon-netbook:~$ echo $$
					18310
					gordon@gordon-netbook:~$ 			
			*/			
			int oldLastNewline = lastNewline;
			attempts = 0;
			while (lastNewline == oldLastNewline && attempts++ < 20) {
				Thread.sleep(testCommandTimeoutMillis);
				firstOutput = firstOutput + readTerminalOutput(in);
				lastNewline = firstOutput.lastIndexOf('\n');
			}
			extractorMessage = extractor.extractPromptAndProcessId(firstOutput, lastNewline);
			if (!"OK".equals(extractorMessage)) {
				throw new MonitorRuntimeException(String.format("Expected a processId and prompt in reply from %s. Test command 'echo $$' returned: %s\nDebug info: %s", server.getHost(), firstOutput, extractorMessage));
			}
		}
		bashProcessId = extractor.getBashProcessId();
		prompt = extractor.getPrompt();
	}



	/** see {@link ch.ethz.ssh2.channel.ChannelInputStream} */
	private String readTerminalOutput(InputStream in) throws Exception {
		int len = 0;
		if (in.available() > 0) {  // <---  we don't want to block here because ServerSessionPool.getSession is synchronized 
			len = in.read(buff);
			return new String(buff, 0, len);
		} else {
			return "";
		}
	}

	public void createAndStartInputFromShellReader(InputStream inputFromShell) {
		inputFromSSHReader = new InputFromSSHReader(in, this, getSessionId());
		inputFromSSHReaderThread = new Thread(inputFromSSHReader, server.getHost() + "-PID:" + bashProcessId);
		inputFromSSHReaderThread.start();
	}


	/** SSHExecuter is pure java so does not have a separate process but we need to stop the reader thread and release the ssh resource. */
	public void killProcess() {
		if (alreadyKilled)
			return;
		ArrayList<Exception> exceptions = new ArrayList<Exception>();
		alreadyKilled = true;		
		try {
			stopReaderThread();
		} catch (Exception e) {
			//e.fillInStackTrace();
			exceptions.add(e);
		}
		try {		
			terminal.destroy();
		} catch (Exception e) {
			//e.fillInStackTrace();
			exceptions.add(e);
		}
		if (exceptions.size() > 0) {
			StringBuilder message = new StringBuilder();
			for (Exception e : exceptions) {
				logger.log(Level.SEVERE, "Problem when ending SSHExecuter process.", e);
				message.append(e.getMessage());
				if (exceptions.size() > 1) {
					message.append('\n');
				}
			}
			throw new MonitorRuntimeException(message.toString());
		}
	}
	
	public void stopReaderThread() throws InterruptedException {
		CountDownLatch stoppedSignal = new CountDownLatch(1);
		inputFromSSHReader.stopRunning(stoppedSignal);
		inputFromSSHReader.addCommandToQueue(commandCounter++, new Command(CommandAndNumber.WAKE_UP));
		boolean stopped = stoppedSignal.await(1000, TimeUnit.MILLISECONDS);
		if (!stopped) {
			inputFromSSHReaderException = null;
			inputFromSSHReaderThread.interrupt();
			throw new MonitorRuntimeException("Thread " + inputFromSSHReaderThread.getName() + " did not stop when asked.");
		}
	}	
	

	public synchronized void resetChunkedOutput(String newSessionId, int millisBeforeInterupting) throws Exception {
		this.sessionId = newSessionId;
		inputFromSSHReader.changeSessionId(newSessionId);
		flushInputFromShell();
		inputFromSSHReader.clearChunkedOutput();
		lastChunkRead = -1;
	}
	
	/**
	 * This is the original method called when closing the session. We have to throw an exception when we have failed to 
	 * make the inputFromSSHReader stop reading. This makes session do a logout which prevents a
	 * deadlock from occuring when testTerminal is called. We don't start a new reader thread
	 * because this would cause a thread plague when something goes wrong.
	 * @see monitor.implementation.shell.CommandExecuter#resetChunkedOutput()
	 * 
	 * But I don't think we can interrupt the thread when it is blocked on a read. see java.nio.channels.InterruptibleChannel
	 * Even if we could wouldn't the channel be closed?
	 */
	public synchronized void resetChunkedOutputOriginal(String newSessionId, int millisBeforeInterupting) throws Exception {
		this.sessionId = newSessionId;
		inputFromSSHReader.changeSessionId(newSessionId);
		CountDownLatch stopFinishedCheckingSignal = new CountDownLatch(1);		
		inputFromSSHReader.stopFinishedChecking(stopFinishedCheckingSignal);
		inputFromSSHReader.addCommandToQueue(commandCounter++, new Command(CommandAndNumber.WAKE_UP));
		boolean stopped = stopFinishedCheckingSignal.await(millisBeforeInterupting, TimeUnit.MILLISECONDS);
		if (!stopped) {
			exceptionSignal = new CountDownLatch(1);
			inputFromSSHReaderThread.interrupt();
			boolean interrupted = exceptionSignal.await(5000, TimeUnit.MILLISECONDS);
			if (!interrupted) {
				throw new MonitorRuntimeException("Thread " + inputFromSSHReaderThread.getName() + " may not have been interrupted.");	
			} else {
				String stackTrace = StackTraceFormatter.asString(inputFromSSHReaderException);
				logger.log(Level.INFO, "Thread " + inputFromSSHReaderThread.getName() + " sucessfully interrupted.\n" + stackTrace);
			}
		}
		flushInputFromShell();
		inputFromSSHReader.clearChunkedOutput();
		lastChunkRead = -1;
	}	
	
	public void saveInputFromShellReaderException(Exception e) {
		inputFromSSHReaderException = e;
		exceptionSignal.countDown();
	}	
	
	public void flushInputFromShell() throws Exception {
		int available;
		try {
			out.write("echo $$\n".getBytes());
			out.flush();
			Thread.sleep(testCommandTimeoutMillis);
			available = in.available(); // checking first avoids blocking on the read
			while (available > 0) {
				in.read(new byte[available]);
				available = in.available();
			}
		} catch (IOException e) {
			throw new MonitorRuntimeException(e);
		}
	}	
	
	public void stopReadingInput() {
		inputFromSSHReader.stopFinishedChecking(new CountDownLatch(1));
	}
	
	public synchronized void clearChunkedOutput() {
		inputFromSSHReader.clearChunkedOutput();
		lastChunkRead = -1;
	}
	
	public void writeToShell(String text) {
		try {
			out.write(text.getBytes());
			out.flush();			
		} catch (IOException e) {
			logger.log(Level.SEVERE, String.format("problem writing text to shell on %s", server), e);
			throw new MonitorRuntimeException(e);
		}
	}
	
	
	public CommandResult executeCommand(Command command) {
		String cmd = command.getRequest().concat("\n");
		String response = "";
		// write the command
		try {
			// write this command to the shells input
			out.write(cmd.getBytes());
			out.flush();
			long startTime = System.currentTimeMillis();
			if ((command.getRequest().equals("exit") || command.getRequest().equals("logout"))) {
				lastCommandStatus = CommandStatus.FINISHED;
				killProcess();
			} else {
				lastCommandStatus = CommandStatus.RUNNING;
				try {
					wantToBeInterupted = true;
					inputFromSSHReader.setSSHExecuterThread(Thread.currentThread());
					// wake up the thread that reads output
					inputFromSSHReader.addCommandToQueue(commandCounter++, command);
					Thread.sleep(command.getMillisBeforeTimeout());
					wantToBeInterupted = false;
				} catch (InterruptedException ie) {
					// good, we were interrupted by InputFromShellReader thread which found output before we gave up waiting
					if (logFine) logger.info("Interupted after " + (System.currentTimeMillis() - startTime) + "ms while waiting because got output from command: " + cmd);
				}
				// but - InputFromShellReader now has our thread and could interrupt anytime so wantsToBeInterupted = false
				wantToBeInterupted = false;
			}			
			int chunkAfter = getChunkedOutput().getHighestChunkNumber();
			response = getChunkedOutput().getChunks(lastChunkRead + 1, chunkAfter);
			lastChunkRead = chunkAfter;
			
		} catch (Exception e) {
			if (!command.getRequest().equals("exit") && !command.getRequest().equals("logout")) {
				logger.log(Level.SEVERE, String.format("problem executing command: '%s' on %s", command.getRequest(), server), e);
			}
			throw new MonitorRuntimeException(e);
		}
		if (inputFromSSHReaderException != null) {
			throw new MonitorRuntimeException("InputFromSSHReader threw exception: " + inputFromSSHReaderException.getMessage());
		}
		CommandResult commandResult = new CommandResult();
		commandResult.setCommandStatus(lastCommandStatus);
		commandResult.setSessionId(getSessionId());
		commandResult.setOutput(response);
		commandResult.setChunkNumber(lastChunkRead);
		return commandResult;
	}
	
	
	public void appendToChunkedOutput(String chunk) {
		if (getChunkedOutput().getHighestChunkNumber() > lastChunkRead ) {
			// can't stop the chunk from being included after some command output that has not been processed yet
		} else {
			// wont duplicate the chunk in the next commands output
			lastChunkRead++;   
		}
		inputFromSSHReader.getChunkedOutput().append(chunk);
	}

	public int getBashProcessId() {
		return bashProcessId;
	}


	public ChunkedOutput getChunkedOutput() {
		return inputFromSSHReader.getChunkedOutput();
	}


	public CommandStatus getLastCommandStatus() {
		return lastCommandStatus;
	}


	public String getRunningCommand() {
		return inputFromSSHReader.getRunningCommand();
	}


	public String getSessionId() {
		return sessionId;
	}


	public boolean isInteruptable() {
		return wantToBeInterupted;
	}


	public void copyOutputTo(ChunkedOutput chunkedOutput) {
		inputFromSSHReader.copyOutputTo(chunkedOutput);
	}

	/**This is superfluous here because it is set in the constructor. */
	
	public void setBashProcessId(int bashProcessId) {
		this.bashProcessId = bashProcessId;
		inputFromSSHReaderThread.setName(server.getHost() + "-PID:" + bashProcessId);
	}

	
	public void setLastCommandStatus(CommandStatus status) {
		lastCommandStatus = status;
	}

	public String getPrompt() {
		return prompt;
	}
	
	
	public String getLoggedOnUserName() {
		return terminal.getLoggedOnUserName();
	}

	
	public String getLoggedOnUserPassword() {
		return terminal.getLoggedOnUserPassword();
	}

	
	public String getHostName() {
		return server.getHost();
	}

	/** for tests */
	public InputFromSSHReader getInputFromSSHReader() {
		return inputFromSSHReader;
	}
	
}
