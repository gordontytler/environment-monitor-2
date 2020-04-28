package monitor.api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import monitor.implementation.MonitorNoStackTraceRuntimeException;

public class StackTraceFormatter {

	static Logger logger = Logger.getLogger(StackTraceFormatter.class.getName());	

	public static final String asString(Exception e) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		StackTraceFormatter.getStackTrace(e, out);
		return out.toString();
	}
	
	public static void getStackTrace(Exception e, ByteArrayOutputStream out)  {
		if (e instanceof MonitorNoStackTraceRuntimeException) {
			try {
				out.write(e.getMessage().getBytes());
			} catch (IOException e1) {
				logger.log(Level.SEVERE, "Problem formatting exception.", e);				
			}
		} else {
			PrintStream s = new PrintStream(out, true);
			e.printStackTrace(s);
		}
	}
	
	
	
}
