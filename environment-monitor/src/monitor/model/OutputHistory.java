package monitor.model;

/** An array of this can be used to draw a graph. */
public class OutputHistory {

	private int lines;
	private int severity;
	private int bytes;
	
	public int getLines() {
		return lines;
	}
	public void setLines(int lines) {
		this.lines = lines;
	}
	public int getSeverity() {
		return severity;
	}
	public void setSeverity(int severity) {
		this.severity = severity;
	}
	public int getBytes() {
		return bytes;
	}
	public void setBytes(int bytes) {
		this.bytes = bytes;
	}
	@Override
	public String toString() {
		return "OutputHistory [bytes=" + bytes + ", lines=" + lines
				+ ", severity=" + severity + "]";
	}

	
	
}
