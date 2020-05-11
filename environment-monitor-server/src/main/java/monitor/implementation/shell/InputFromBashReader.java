package monitor.implementation.shell;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import monitor.implementation.MonitorRuntimeException;
import monitor.model.Command;
import monitor.model.CommandStatus;
import monitor.model.Configuration;

/** @deprecated use InputFromSSHReader instead. */
public class InputFromBashReader implements Runnable {
	
	static Logger logger = Logger.getLogger(InputFromBashReader.class.getName());

	private static final int INPUT_POLL_MILLIS = 333; // <-- this is a drastic effect on CPU usage due to BufferedInputStream.available()
	
	// With a queue the executer can pump out commands and expect the status to be finished when the queue is empty. 
	// But, in practice, you don't need this for a series of quick commands and if one command never finishes or takes
	// a long time the queue fills up. Depending on the implementation it either blocks or errors when full.
	// The queue is now only used to wake up the reader.
	
	private static final int COMMAND_QUEUE_SIZE = 1;  
	
	private BlockingQueue<CommandAndNumber> commandQueue = new ArrayBlockingQueue<CommandAndNumber>(COMMAND_QUEUE_SIZE);
	private InputStream inputFromShell;
	private ChunkedOutput chunkedOutput = new ChunkedOutput(Configuration.getInstance().getChunkedOutputArraySize());	
	private BashExecuter bashExecuter;
	private String runningRequest;	
	private Integer commandNumber;
	private Thread bashExecuterThread;

	private String sessionToken;
	private String tokenForLog;
	private int endBytesMatchPosition = 0;	
	private boolean foundSessionToken = false;	
	private boolean foundEchoText = false;
	private CommandNumberExtractor commandNumberExtractor = new CommandNumberExtractor();

	private CountDownLatch stoppedSignal = null;
	private CountDownLatch stopFinishedCheckingSignal = null;	

	
	InputFromBashReader(InputStream inputFromShell, BashExecuter bashExecuter, String sessionId) {
		this.inputFromShell = inputFromShell;
		this.bashExecuter = bashExecuter;
		this.sessionToken = "z" + sessionId;
		this.tokenForLog = "zed" + sessionId; // prevent confusion when reading its own log
	}
	
	
	void addCommandToQueue(int commandNumber, Command command) {
		if (logger.isLoggable(Level.FINE)) logger.fine(String.format("sessionToken:%s put command %d - '%s' on blocking queue .", sessionToken, commandNumber, command));
		boolean notFull = commandQueue.offer(new CommandAndNumber(commandNumber, command));
		if (!notFull) {
			MonitorRuntimeException queueFull = new MonitorRuntimeException(String.format("sessionToken:%s Could not add command '%s' to queue because it full.\n" +
					"%d commands waiting behing the currently executing command '%s'.", sessionToken, command, COMMAND_QUEUE_SIZE, runningRequest));
			bashExecuter.saveInputFromShellReaderException(queueFull);
			bashExecuter.setLastCommandStatus(CommandStatus.ERROR);
		}
	}

	
	public void stopRunning(CountDownLatch stoppedSignal) {
		this.stoppedSignal = stoppedSignal;
		if (this.stopFinishedCheckingSignal == null) {
			stopFinishedChecking(new CountDownLatch(1));
		}
	}	

	
	int takeNextCommand() throws InterruptedException {
		if (logger.isLoggable(Level.FINE)) logger.fine(String.format("sessionToken:%s getting next command from blocking queue...", tokenForLog));
		CommandAndNumber commandAndNumber = commandQueue.take(); // <-- it blocks here
		if (stoppedSignal != null || stopFinishedCheckingSignal != null) {
			if (logger.isLoggable(Level.FINE)) logger.fine(String.format("sessionToken:%s stoppedSignal is %b stopFinishedCheckingSignal is %b", tokenForLog, (stoppedSignal != null), (stopFinishedCheckingSignal != null)));
			return 0;
		}
		// when the previous session was closed we may have found the stopLookingForPromptSignal inside the readInputFromShell loop
		// so we were not blocked waiting for a command and the wake up command was left on the queue - so just ignore it 
		if (CommandAndNumber.WAKE_UP.equals(commandAndNumber.getRequest())) {
			commandAndNumber = commandQueue.take();
		}
		commandNumber = commandAndNumber.getCommandNumber();
		runningRequest = commandAndNumber.getRequest();
		if (logger.isLoggable(Level.FINE)) logger.fine(String.format("sessionToken:%s[%d] - '%s' taken from blocking queue.", sessionToken, commandNumber, runningRequest));
		return commandAndNumber.getCommandNumber();
	}	
	
	
	
	public void run() {
		try {
			foundEchoText = false;
			// block until BashExecuter adds to the queue
			commandNumber = takeNextCommand();
			
			while (stoppedSignal == null) {
				if (logger.isLoggable(Level.FINE)) logger.fine(String.format("sessionToken:%s Looking for echoText: %s", tokenForLog, formatEchoTextForLog(commandNumber)));
				while (!foundEchoText && stopFinishedCheckingSignal == null) {
					String output = readInputFromShell(INPUT_POLL_MILLIS, commandNumber);
					if (output != null && output.length() > 0) {
						chunkedOutput.append(output);	
					}
				}
				if (logger.isLoggable(Level.FINE)) logger.fine(String.format("sessionToken:%s foundEchoText=%b stopLookingForEchoText=%b - %s", tokenForLog, foundEchoText, stopFinishedCheckingSignal != null ? true : false, formatEchoTextForLog(commandNumber).trim()));
				logger.info(String.format("sessionToken:%s[%d] finished - '%s'", tokenForLog, commandNumber, runningRequest));
				if (commandQueue.isEmpty()) {
					if (logger.isLoggable(Level.FINE)) logger.fine(String.format("sessionToken:%s FINISHED %s", tokenForLog, formatEchoTextForLog(commandNumber).trim()));
					bashExecuter.setLastCommandStatus(CommandStatus.FINISHED);
				}
				if (bashExecuterThread != null &&  bashExecuter != null && bashExecuterThread.getState() == Thread.State.TIMED_WAITING && bashExecuter.isInteruptable()) {
					bashExecuterThread.interrupt();
				}
				if (stopFinishedCheckingSignal != null) {
					stopFinishedCheckingSignal.countDown();
					stopFinishedCheckingSignal = null;
				}
				// block until BashExecuter adds to the queue
				commandNumber = takeNextCommand();
				foundEchoText = false;				
			}
			if (logger.isLoggable(Level.FINE)) logger.fine(String.format("sessionToken:%s InputFromShellReader thread ending normally.", tokenForLog));
			stoppedSignal.countDown();
			
		} catch (Exception e) {
			bashExecuter.saveInputFromShellReaderException(e);
			bashExecuter.setLastCommandStatus(CommandStatus.ERROR);
		}
	}

	// This must set foundEchoText or the Thread will loop forever.
	private String readInputFromShell(int millisToWait, int commandNumber) throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int totalTime = 0;
		byte[] endBytes = sessionToken.getBytes(); // it is usually at the end but can be at the beginning
		foundSessionToken = false;
		endBytesMatchPosition = 0;


		while (totalTime <= millisToWait && !foundEchoText) {
			while (inputFromShell.available() > 0) {
				char c = (char) inputFromShell.read();
				out.write(c);
				if (!foundSessionToken) {
					if (c == endBytes[endBytesMatchPosition++]) {
						if (endBytesMatchPosition == endBytes.length) {
							if (logger.isLoggable(Level.FINE)) logger.fine(String.format("sessionToken:%s Found sessionToken in response.", tokenForLog));
							endBytesMatchPosition = 0;
							foundSessionToken = true;
						}
					} else {
						endBytesMatchPosition = 0;
					}
				} else {
					// When the COMMAND_QUEUE_SIZE was > 1 a whole bunch could queue up and then all end at the same time
					// so it was necessary to know the command number to determine if the current one had finished.
					CommandNumberExtractorResult result = commandNumberExtractor.extractNumberInSquareBrackets(c);
					switch (result) {
					case STILL_LOOKING: 
						break;
					case FOUND_NUMBER: 
						foundEchoText = checkCommandQueueUpTo(commandNumberExtractor.getNumberInBrackets());
						commandNumberExtractor.reinitialise();
						foundSessionToken = false; // there might be another
						break;
					case NO_NUMBER_FOUND:
						commandNumberExtractor.reinitialise();						
						foundSessionToken = false; // there might be another
						break;
					}
				}
			}
			if (INPUT_POLL_MILLIS + totalTime < millisToWait) {
				Thread.sleep(INPUT_POLL_MILLIS);
			}
			totalTime += INPUT_POLL_MILLIS;			
		}
		String inputFromShell = out.toString(); 
		if (foundEchoText) {
			if (logger.isLoggable(Level.FINE)) logger.fine(String.format("sessionToken:%s Found echo text in response: %s", tokenForLog, inputFromShell));
		}
		return removeEchoText(inputFromShell);		
	}


	/** @return true if the command number echoed back is greater than or equal to the current command.
	 * <br>false if command that has finished is before the current one or there are more in the queue.
	 * Serves no purpose is queue length is 1.
	 * @throws InterruptedException */
	private boolean checkCommandQueueUpTo(Integer numberInSquareBrachets) throws InterruptedException {
		if (numberInSquareBrachets == commandNumber) {
			if (logger.isLoggable(Level.FINE)) logger.fine(String.format("sessionToken:%s returning true because numberInSquareBrachets %d == %d", tokenForLog, numberInSquareBrachets, commandNumber));
			return true;
		} else if (numberInSquareBrachets < commandNumber) {
			if (logger.isLoggable(Level.FINE)) logger.fine(String.format("sessionToken:%s returning false because numberInSquareBrachets %d < %d", tokenForLog, numberInSquareBrachets, commandNumber));			
			return false;
		} else {
			while (!commandQueue.isEmpty() && numberInSquareBrachets > commandNumber) {
				commandNumber = takeNextCommand();
				if (logger.isLoggable(Level.FINE)) logger.fine(String.format("sessionToken:%s numberInSquareBrachets is %d took command %d from queue", tokenForLog, numberInSquareBrachets, commandNumber));
			}
			if (commandQueue.isEmpty()) {
				if (logger.isLoggable(Level.FINE)) logger.fine(String.format("sessionToken:%s returning true numberInSquareBrachets is %d and command is %d ", tokenForLog, numberInSquareBrachets, commandNumber));
				return true;
			}
			// there is a command in the queue after the one we know has finished
			if (logger.isLoggable(Level.FINE)) logger.fine(String.format("sessionToken:%s returning true because '%s' is in the queue, numberInSquareBrachets is %d and command is %d ", tokenForLog, commandQueue.peek().getCommand(), numberInSquareBrachets, commandNumber));
			return true;
		}
	}


	/**
	 * Remove all string that begin with the sessionToken and end with commandNumber in square brackets e.g. z14903[19] 
	 * @param input
	 * @return
	 */
	String removeEchoText(String input) {
		byte[] token = sessionToken.getBytes();
		byte lastTokenByte = token[token.length -1];
		byte[] in = input.getBytes();
		char[] out = new char[in.length];
		int f = 0;
		int outIndex = 0;
		
		while(f < in.length) {
			if (in[f] == token[0]) {
				int tokenEnd = f + token.length - 1;
				if (tokenEnd <= in.length && in[tokenEnd] == lastTokenByte) {
					boolean allBytesFound = true;
					for (int b = 0; b < token.length; b++) {
						if (token[b] != in[f + b]) {
							allBytesFound = false;
							break;
						}
					}
					if (allBytesFound) {
						f = f + token.length;
						if (f < in.length && in[f] == '[') {
							int x = f + 1;
							while (x < in.length && in[x] >= '0' && in[x] <= '9') {
								x++;
							}
							if (in[x] == ']') {
								x++;
								if (x < in.length && '\n' == in[x]) {
									x++;
								}
								f = x;
							}
						}
					}
				}
			}
			if (f < in.length) {
				out[outIndex++] = (char)in[f++]; 
			}
		}
		return new String(out, 0, outIndex);
	}	
	
	void sendOutputTo(ChunkedOutput chunkedOutput) {
		this.chunkedOutput = chunkedOutput;
	}
	
	void clearChunkedOutput() {
		this.chunkedOutput = new ChunkedOutput(Configuration.getInstance().getChunkedOutputArraySize());
	}
	
	ChunkedOutput getChunkedOutput() {
		return chunkedOutput;
	}

	String formatEchoText(int commandNumber) {
		return String.format("%s[%d]\n", sessionToken, commandNumber);
	}

	/** echo text has to be different in log files incase we are monitoring this applications log file */
	String formatEchoTextForLog(int commandNumber) {
		return String.format("%s[%d]\n", tokenForLog, commandNumber);
	}	
	
	String getRunningCommand() {
		return runningRequest;
	}

	void setBashExecuterThread(Thread bashExecuterThread) {
		this.bashExecuterThread = bashExecuterThread;
	}
	

	public void changeSessionId(String newSessionId) {
		logger.info(String.format("sessionToken:%s this sessionToken has been replaced with new sessionToken:%s", tokenForLog, "zed" + newSessionId));
		this.sessionToken = "z" + newSessionId;
		this.tokenForLog = "zed" + newSessionId; // prevent false detection of finished command when reading its own log
	}

	public void stopFinishedChecking(CountDownLatch stopFinishedCheckingSignal) {
		this.stopFinishedCheckingSignal = stopFinishedCheckingSignal;
	}	

}
