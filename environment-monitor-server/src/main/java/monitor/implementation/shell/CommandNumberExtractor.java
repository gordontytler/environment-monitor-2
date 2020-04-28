package monitor.implementation.shell;

import static monitor.implementation.shell.CommandNumberExtractorResult.FOUND_NUMBER;
import static monitor.implementation.shell.CommandNumberExtractorResult.NO_NUMBER_FOUND;
import static monitor.implementation.shell.CommandNumberExtractorResult.STILL_LOOKING;

import java.util.logging.Level;
import java.util.logging.Logger;

import monitor.implementation.MonitorRuntimeException;


public class CommandNumberExtractor {

	static Logger logger = Logger.getLogger(CommandNumberExtractor.class.getName());	
	
	Integer numberInBrackets;
	private int echoedCommandNumberCount = 0;
	private StringBuilder echoedCommandNumberBuilder = new StringBuilder();

	public void reinitialise() {
		echoedCommandNumberCount = 0;
		echoedCommandNumberBuilder = new StringBuilder();
		numberInBrackets = null;
	}
	
	public CommandNumberExtractorResult extractNumberInSquareBrackets(char c) {
		echoedCommandNumberCount++;
		
		logger.log(Level.FINEST, String.format("'%c' at %d", c, echoedCommandNumberCount));
		
		if (echoedCommandNumberCount == 1) {
			if (c == '[') {
				return STILL_LOOKING;
			} else {
				return NO_NUMBER_FOUND;
			}
		}
		// we have found the last character of the session token followed by '['
		if (c >= '0' && c <= '9') {
			// c used to be int so perhas this isnt necessary 
			//echoedCommandNumberBuilder.append(new String(new char[] {c}));
			echoedCommandNumberBuilder.append(c);
		} else {
			if (c == ']') {
				numberInBrackets = new Integer(echoedCommandNumberBuilder.toString());
				logger.log(Level.FINE, String.format("commandNumber is %d", numberInBrackets));
				return FOUND_NUMBER;
			} else {
				return NO_NUMBER_FOUND;
			}
		}
		if (echoedCommandNumberCount > 5) {
			throw new MonitorRuntimeException(String.format("command number in echoText is too big: %s", echoedCommandNumberBuilder.toString()));	
		}
		return STILL_LOOKING;
	}

	public Integer getNumberInBrackets() {
		return numberInBrackets;
	}
	
}
