package monitor.implementation;

/** Using this exception is a hint that the stack trace is not needed. */
public class MonitorNoStackTraceRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 6128081654704339400L;

	public MonitorNoStackTraceRuntimeException() {
	}

	public MonitorNoStackTraceRuntimeException(String message) {
		super(message);
	}

	public MonitorNoStackTraceRuntimeException(Throwable cause) {
		super(cause);
	}

	public MonitorNoStackTraceRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

}
