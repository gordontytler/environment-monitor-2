package monitor.dao;

public class LineNumber {
	private int line;
	public void increment() {
		line++;
	}
	public int getNumber() {
		return line;
	}
	@Override
	public String toString() {
		return new Integer(line).toString();
	}
}
