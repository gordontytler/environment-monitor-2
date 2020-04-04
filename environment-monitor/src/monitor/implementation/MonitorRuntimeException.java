package monitor.implementation;

public class MonitorRuntimeException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3165318473475149291L;

	public MonitorRuntimeException() {
	}

	public MonitorRuntimeException(String message) {
		super(message);
	}

	public MonitorRuntimeException(Throwable cause) {
		super(cause);
	}

	public MonitorRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

}
