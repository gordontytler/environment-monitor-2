package monitor.implementation.shell;

import monitor.implementation.MonitorRuntimeException;
import monitor.model.Command;
import monitor.model.CommandStatus;
import monitor.model.Configuration;
import monitor.model.StringUtil;

import java.io.InputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InputFromSSHReader implements Runnable {

    private static final int INPUT_POLL_MILLIS = 10;  // unlike BashExecuter this has little impact on cpu
    private static final int COMMAND_QUEUE_SIZE = 1;  // the queue idea was not such a good idea because it gets stuck
    static Logger logger = Logger.getLogger(InputFromSSHReader.class.getName());
    private boolean logFine = Configuration.getInstance().isLogFine();
    private boolean logFineFilter = Configuration.getInstance().isLogFineSomething("InputFromSSHReader.filter");
    private final BlockingQueue<CommandAndNumber> commandQueue = new ArrayBlockingQueue<CommandAndNumber>(COMMAND_QUEUE_SIZE);
    private final InputStream in;
    private ChunkedOutput chunkedOutput = new ChunkedOutput(Configuration.getInstance().getChunkedOutputArraySize());
    private final PasswordTyper passwordTyper = new PasswordTyper();
    private final SSHExecuter sshExecuter;
    private String sessionId;
    private String runningRequest;
    private Integer commandNumber;
    private int lastChunkRead = -1;

    private boolean foundPrompt = false;
    private boolean foundEchoedCommand = false;
    private final byte[] buff = new byte[8192];

    private Thread sshExecuterThread;

    private final String prompt;
    private final int promptLength;
    private final byte[] promptBytes;

    private CountDownLatch stopSignal = null;
    private CountDownLatch stopFinishedCheckingSignal = null;
    private ChunkedOutput copyOfChunkedOutput = null;


    InputFromSSHReader(InputStream in, SSHExecuter sshExecuter, String sessionId) {
        this.in = in;
        this.sshExecuter = sshExecuter;
        this.sessionId = sessionId;
        this.prompt = sshExecuter.getPrompt();
        this.promptLength = prompt.length();
        this.promptBytes = prompt.getBytes();
    }

    void addCommandToQueue(int commandNumber, Command command) {
        if (logger.isLoggable(Level.FINE))
            logger.fine(String.format("sessionId:%s put command %d - '%s' on blocking queue .", sessionId, commandNumber, command));
        boolean notFull = commandQueue.offer(new CommandAndNumber(commandNumber, command));
        if (!notFull) {
            MonitorRuntimeException queueFull = new MonitorRuntimeException(String.format("Could not add command '%s' to queue because it full.\n" +
                    "%d commands waiting behing the currently executing command '%s'.", command, COMMAND_QUEUE_SIZE, runningRequest));
            sshExecuter.saveInputFromShellReaderException(queueFull);
            sshExecuter.setLastCommandStatus(CommandStatus.ERROR);
        }
    }

    public void stopRunning(CountDownLatch stopSignal) {
        this.stopSignal = stopSignal;
        if (this.stopFinishedCheckingSignal == null) {
            stopFinishedChecking(new CountDownLatch(1));
        }
    }

    CommandAndNumber takeNextCommandAndNumber() throws InterruptedException {
        if (logger.isLoggable(Level.FINE))
            logger.fine(String.format("sessionId:%s getting next command from blocking queue...", sessionId));
        CommandAndNumber commandAndNumber = commandQueue.take(); // <-- it blocks here
        if (stopSignal != null || stopFinishedCheckingSignal != null) {
            if (logger.isLoggable(Level.FINE))
                logger.fine(String.format("sessionId:%s stoppedSignal is %b stopFinishedCheckingSignal is %b", sessionId, (stopSignal != null), (stopFinishedCheckingSignal != null)));
            return null;
        }
        // when the previous session was closed we may have found the stopLookingForPromptSignal inside the readInputFromShell loop
        // so we were not blocked waiting for a command and the wake up command was left on the queue - so just ignore it
        if (CommandAndNumber.WAKE_UP.equals(commandAndNumber.getRequest())) {
            commandAndNumber = commandQueue.take();
        }
        commandNumber = commandAndNumber.getCommandNumber();
        runningRequest = commandAndNumber.getRequest();
        if (logger.isLoggable(Level.FINE))
            logger.fine(String.format("sessionId:%s[%d] - '%s' taken from blocking queue.", sessionId, commandNumber, runningRequest));
        return commandAndNumber;
    }


    public void run() {
        try {
            foundPrompt = false;
            // block until SSHExecuter adds to the queue
            CommandAndNumber commandAndNumber = takeNextCommandAndNumber();

            while (stopSignal == null) {
                if (logger.isLoggable(Level.FINE))
                    logger.fine(String.format("sessionId:%s[%d] Looking for prompt: %s", sessionId, commandNumber, sshExecuter.getPrompt()));
                String output = "";
                StringBuilder allOutput = new StringBuilder();
                while (!foundPrompt && stopFinishedCheckingSignal == null) {
                    output = readInputFromShell(INPUT_POLL_MILLIS, commandAndNumber.getCommand());
                    if (output != null && output.length() > 0) {
                        if (commandAndNumber.getCommand().isStripEchoedCommandFromResponse()) {
                            allOutput.append(output);
                        } else {
                            appendTochunkedOutput(output);
                        }
                    }
                } // TODO - rewrite this or delete because SSHExecuter says  out.write("stty -echo\n".getBytes());
                if (commandAndNumber != null && commandAndNumber.getCommand().isStripEchoedCommandFromResponse()) {
                    appendTochunkedOutput(stripEchoedCommand(allOutput, commandAndNumber.getRequest()));
                }
                if (logger.isLoggable(Level.FINE))
                    logger.fine(String.format("sessionId:%s[%d] foundPrompt=%b stopLookingForPrompt=%b - %s", sessionId, commandNumber, foundPrompt, stopFinishedCheckingSignal != null, safeSubString(runningRequest, 30)));
                // This is the INFO level message
                logger.info(String.format("sessionId:%s[%d] finished - '%s' -> %s", sessionId, commandNumber, safeSubString(runningRequest, 30), safeSubString(getChunksForLogMessage(), 120)));
                if (commandQueue.isEmpty()) {
                    if (logFine)
                        logger.info(String.format("sessionId:%s[%d] commandQueue.isEmpty CommandStatus is FINISHED %s", sessionId, commandNumber, safeSubString(runningRequest, 30)));
                    sshExecuter.setLastCommandStatus(CommandStatus.FINISHED);
                }
                if (sshExecuterThread != null && sshExecuter != null && sshExecuterThread.getState() == Thread.State.TIMED_WAITING && sshExecuter.isInteruptable()) {
                    sshExecuterThread.interrupt();
                }
                if (stopFinishedCheckingSignal != null) {
                    stopFinishedCheckingSignal.countDown();
                    stopFinishedCheckingSignal = null;
                }
                // block until SSHExecuter adds to the queue again
                commandAndNumber = takeNextCommandAndNumber();
                foundPrompt = false;
                foundEchoedCommand = false;
            }
            if (logFine)
                logger.info(String.format("sessionId:%s[%d] InputFromSSHReader thread ending normally.", sessionId, commandNumber));
            stopSignal.countDown();
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage() + " in thread " + Thread.currentThread().getName(), e);
            e.fillInStackTrace();
            sshExecuter.saveInputFromShellReaderException(e);
            sshExecuter.setLastCommandStatus(CommandStatus.ERROR);
        }
    }

    /**
     * Used when merging script output and output from sessions on different hosts.
     */
    private void appendTochunkedOutput(String chunk) {
        chunkedOutput.append(chunk);
        if (copyOfChunkedOutput != null) {
            copyOfChunkedOutput.append(chunk);
        }
    }

    /**
     * Read the reply from the remote host. This method blocks until input data is available.
     * Check for the terminal prompt. This means the command has finished. SSHExecuter will do "stty -echo"
     * so there is no need to strip the echoed command (although the code below still does this).
     * Neither of these are guaranteed to work because InputStream.read could chop at the wrong place.
     *
     * @param command
     */
    private String readInputFromShell(int inputPollMillis, Command command) throws Exception {
        Thread.sleep(inputPollMillis);
        int len = in.read(buff); // This will block and I don't think it can be interrupted.
        if (len > 0 && command.mightRequirePassword()) {
            // the password for sudo
            if (passwordTyper.typePasswordIfAsked(sshExecuter, len, buff, command)) {
                logger.info(String.format("sessionId:%s[%d] Password provided for user %s running command: %s", sessionId, commandNumber, sshExecuter.getLoggedOnUserName(), safeSubString(runningRequest, 30)));
                command.setPasswordAlreadyTyped(true);
                Thread.sleep(inputPollMillis);
                len = in.read(buff);
                if (len > 0 && command.mightRequireRetypePassword()) {
                    // the password for changing a password
                    if (passwordTyper.typePasswordIfAsked(sshExecuter, len, buff, command)) {
                        logger.info(String.format("sessionId:%s[%d] Password retyped for user %s running command: %s", sessionId, commandNumber, sshExecuter.getLoggedOnUserName(), safeSubString(runningRequest, 30)));
                        Thread.sleep(inputPollMillis);
                        len = in.read(buff);
                    }
                    // Retype new UNIX password:
                    if (len > 0 && passwordTyper.typePasswordIfAsked(sshExecuter, len, buff, command)) {
                        logger.info(String.format("sessionId:%s[%d] Password retyped again for user %s running command: %s", sessionId, commandNumber, sshExecuter.getLoggedOnUserName(), safeSubString(runningRequest, 30)));
                        Thread.sleep(inputPollMillis);
                        len = in.read(buff);
                    }
                }
            }
        }
        return filter(len, buff, command);
    }

    /**
     * Removes the echoed command and prompt leaving just the output.
     */
    String filter(int len, byte[] buff, Command command) throws Exception {
        String filtered = "";
        if (len == -1)
            return "";

        if (logFineFilter)
            logger.info(String.format("sessionId:%s[%d] raw buffer: %s", sessionId, commandNumber, new String(buff, 0, len)));

        int scanStart = 0;
        // strip out a command prompt at the beginning followed by other stuff because it must be a prompt following a previous command
        if (len > promptLength && buff[0] == promptBytes[0] && buff[promptLength - 1] == promptBytes[promptLength - 1]) {
            boolean startsWithPrompt = true;
            for (int p = 1; p < promptLength; p++) {
                if (promptBytes[p] != buff[p]) {
                    startsWithPrompt = false;
                    break;
                }
            }
            if (startsWithPrompt) {
                scanStart = promptLength;
                if (logFineFilter)
                    logger.info(String.format("sessionId:%s[%d] Removed a prompt at beginning that is followed by other stuff.", sessionId, commandNumber));
            }
        }

        // convert into a character array and locate the first return and last newline  TODO - why convert to char[] ?
        int lastNewline = -1;
        int firstReturn = -1;
        char[] chars = new char[len];
        int n, b;
        for (n = 0, b = scanStart; b < len; b++) {
            char c = (char) (buff[b] & 0xff);
            // strip out any terminal control characters such as colours
            if (c < 32 && !(c == '\n' || c == '\r')) { // 8 is Backspace, VERASE but causes SOAP error Message: An invalid XML character (Unicode: 0x8)
                continue;
            }
            chars[n] = c;
            if (c == '\n') {
                lastNewline = n;
            } else if (firstReturn == -1 && c == '\r') {
                firstReturn = n;
            }
            n++;
        }
        len = n;

        // look for the terminal command prompt after the last newline
        if (lastNewline != -1 && lastNewline + promptLength == len - 1) {
            foundPrompt = true;
            for (int p = 0; p < promptLength; p++) {
                if (promptBytes[p] != chars[lastNewline + 1 + p]) {
                    foundPrompt = false;
                    break;
                }
            }
        }

        // look for the command echoed before the first return and set the offset to where it ends
        int offset = 0;
        if (command.isStripEchoedCommandFromResponse() && firstReturn != -1) {
            String uptoFirstReturn = new String(chars, 0, firstReturn); // there will be echoed-command followed by /r/n
            if (uptoFirstReturn.equals(command.getRequest())) {
                offset = firstReturn + 2;
                foundEchoedCommand = true;
            }
        }

        // The echoed command may have been in the previous chunk and the command may have returned no output so all we have is the prompt
        if (lastNewline == -1 && promptLength == len && prompt.equals(new String(chars))) {
            foundPrompt = true;
            filtered = "";
        } else {
            if (foundPrompt) {
                filtered = new String(chars, offset, lastNewline + 1 - offset);
            } else {
                filtered = new String(chars, offset, len - offset);
            }
        }
        if (logFineFilter)
            logger.info(String.format("sessionId:%s[%d] foundPrompt %b offset %d lastNewline %d len %d : %s ", sessionId, commandNumber, foundPrompt, offset, lastNewline, len, filtered));

        return filtered;
    }


    /**
     * Sometimes the command is echoed back split over two lots of output so the code above won't always find it.
     */
    private String stripEchoedCommand(StringBuilder allOutput, String commandRequest) {
        String all = allOutput.toString();
        if (foundEchoedCommand) {
            return all;
        } else {
            int firstReturn = all.indexOf('\r');
            if (firstReturn == -1) {
                return all;
            } else {
                char[] chars = all.toCharArray();
                String uptoFirstReturn = new String(chars, 0, firstReturn);
                if (uptoFirstReturn.equals(commandRequest)) {
                    int offset = firstReturn + 2;
                    foundEchoedCommand = true;
                    return new String(chars, offset, chars.length - offset);
                }
            }
        }
        return all;
    }

    private Object safeSubString(String string, int length) {
        return StringUtil.safeSubString(string, length);
    }

    /**
     * If logFine return all the chunks that were added since this method was last called.
     * If not logFine just return the last one.
     * Try to do the above without causing exceptions or thread blocking.
     */
    private String getChunksForLogMessage() {
        StringBuilder sb = new StringBuilder();
        if (stopSignal == null && stopFinishedCheckingSignal == null) {
            int highestChunkNumber = chunkedOutput.getHighestChunkNumber();
            if (highestChunkNumber > -1) {
                if (logFine) {
                    int fromChunk = chunkedOutput.getLowestChunkNumber();
                    if (lastChunkRead > fromChunk) {
                        fromChunk = lastChunkRead + 1;
                    }
                    String chunks = chunkedOutput.getChunks(fromChunk, highestChunkNumber);
                    lastChunkRead = highestChunkNumber;
                    sb.append(chunks);
                } else {
                    sb.append(chunkedOutput.getChunk(highestChunkNumber));
                }
            }
        } else {
            sb.append((stopFinishedCheckingSignal != null ? " got stopFinishedCheckingSignal " : "") +
                    (stopSignal != null ? "got stopSignal" : ""));
        }
        return sb.toString();
    }

    void copyOutputTo(ChunkedOutput chunkedOutput) {
        this.copyOfChunkedOutput = chunkedOutput;
    }

    void clearChunkedOutput() {
        this.chunkedOutput = new ChunkedOutput(Configuration.getInstance().getChunkedOutputArraySize());
        lastChunkRead = -1;
        this.copyOfChunkedOutput = null;
    }

    ChunkedOutput getChunkedOutput() {
        return chunkedOutput;
    }

    void setSSHExecuterThread(Thread sshExecuterThread) {
        this.sshExecuterThread = sshExecuterThread;
    }

    void stopFinishedChecking(CountDownLatch stopFinishedCheckingSignal) {
        this.stopFinishedCheckingSignal = stopFinishedCheckingSignal;
    }

    public String getRunningCommand() {
        return runningRequest;
    }

    public void changeSessionId(String newSessionId) {
        if (logFine)
            logger.info(String.format("sessionId:%s this sessionId has been replaced with new sessionId:%s", sessionId, newSessionId));
        this.sessionId = newSessionId;
    }

    /**
     * for tests
     */
    public boolean isFoundPrompt() {
        return foundPrompt;
    }

    public void setLogFine(boolean logFine) {
        this.logFine = logFine;
    }

    public void setLogFineFilter(boolean logFineFilter) {
        this.logFineFilter = logFineFilter;
    }


}
