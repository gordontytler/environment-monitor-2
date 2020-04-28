package monitor.model;

public class Command {

	private String request;
	/** may get output when not expected and not get when it is expected */
	private boolean expectOutput = false; 
	private boolean expectContinuousOutput = false;
	private int millisBeforeTimeout = Configuration.getInstance().getDefaultCommandTimeoutMillis();
	/** echo the command in the output here BEFORE sending to the remote server. This is not the same as the remote terminal echoing it back. */
	private boolean localEcho = false;
	private boolean stripEchoedCommandFromResponse = false; // strip out the command if the remote terminal echoes it back
	private boolean passwordAlreadyTyped = false;
	
	public Command(String request) {
		this.request = request;
	}

	public Command(String request, boolean stripEchoedCommandFromResponse) {
		this.request = request;
		this.stripEchoedCommandFromResponse = stripEchoedCommandFromResponse;
	}
	
	/** A shell command without the trailing newline. */
	public String getRequest() {
		return request;
	}
	public void setRequest(String request) {
		this.request = request;
	}
	public boolean isExpectOutput() {
		return expectOutput;
	}
	public void setExpectOutput(boolean expectOutput) {
		this.expectOutput = expectOutput;
	}
	public boolean isExpectContinuousOutput() {
		return expectContinuousOutput;
	}
	public void setExpectContinuousOutput(boolean expectContinuousOutput) {
		this.expectContinuousOutput = expectContinuousOutput;
	}
	public int getMillisBeforeTimeout() {
		return millisBeforeTimeout;
	}
	public void setMillisBeforeTimeout(int millisBeforeTimeout) {
		this.millisBeforeTimeout = millisBeforeTimeout;
	}
	public boolean isLocalEcho() {
		return localEcho;
	}
	public void setLocalEcho(boolean localEcho) {
		this.localEcho = localEcho;
	}
	public boolean isStripEchoedCommandFromResponse() {
		return stripEchoedCommandFromResponse;
	}

	public void setPasswordAlreadyTyped(boolean passwordAlreadyTyped) {
		this.passwordAlreadyTyped = passwordAlreadyTyped;
	}
	
	public boolean mightRequirePassword() {
		return  !passwordAlreadyTyped && (request.startsWith("su ") || request.startsWith("sudo ") || "su".equals(request));
	}

	public boolean mightRequireRetypePassword() {
		return  request.startsWith("sudo passwd ") || request.startsWith("sudo /usr/bin/passwd ");
	}	
	
	
	@Override
	public String toString() {
		return request;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (expectContinuousOutput ? 1231 : 1237);
		result = prime * result + (expectOutput ? 1231 : 1237);
		result = prime * result + (localEcho ? 1231 : 1237);
		result = prime * result + millisBeforeTimeout;
		result = prime * result + ((request == null) ? 0 : request.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Command other = (Command) obj;
		if (expectContinuousOutput != other.expectContinuousOutput)
			return false;
		if (expectOutput != other.expectOutput)
			return false;
		if (localEcho != other.localEcho)
			return false;
		if (millisBeforeTimeout != other.millisBeforeTimeout)
			return false;
		if (request == null) {
			if (other.request != null)
				return false;
		} else if (!request.equals(other.request))
			return false;
		return true;
	}
	
}
