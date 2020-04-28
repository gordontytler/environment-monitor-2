package monitor.implementation.shell;

import org.junit.Test;

@SuppressWarnings("deprecation")
public class InputFromShellReaderTest {

	String sessionId = "1494403";
	
	String input = 	"> echo 3\n" +
	"> echo I killed the process\n" +
	"so now the previous command is still running\n" +
	"z1494403[15]\n" +
	"each new request brings back more of its output\n" +
	"z1494403[16]\n" +
	"2\n" +
	"z1494403[17]\n" +
	"3\n" +
	"z1494403[18]\n" +
	"-bash: line 29: 10101 Terminated              find ./ -name '*log'\n" +
	"I killed the process\n" +
	"z1494403[19]\n" +
	"> When I killed the process it found echo text z1494403-14\n" +
	"-bash: line 41: When: command not found\n" +
	"z1494403[20]\n";
	
	String expected = "> echo 3\n" +
	"> echo I killed the process\n" +
	"so now the previous command is still running\n" +
	"each new request brings back more of its output\n" +
	"2\n" +
	"3\n" +
	"-bash: line 29: 10101 Terminated              find ./ -name '*log'\n" +
	"I killed the process\n" +
	"> When I killed the process it found echo text -14\n" +
	"-bash: line 41: When: command not found\n";

	@Test
	public void testRemoveEchoText() {
		String sessionId = String.format("%d", System.nanoTime());		
		InputFromBashReader inputFromShellReader = new InputFromBashReader(null, null, sessionId);

		String inputWithToken = "abcdefg\n" + "z" + sessionId + "[1234]" + "blah";
		String trimmed = inputFromShellReader.removeEchoText(inputWithToken);
		//assertEquals("abcdefg\nblah", trimmed);		
		assert("abcdefg\nblah".equals(trimmed));
	}

	@Test
	public void testRemoveAllEchoText() {
		InputFromBashReader inputFromShellReader = new InputFromBashReader(null, null, sessionId);
		String trimmed = inputFromShellReader.removeEchoText(input);
		//assertEquals(expected, trimmed);
		assert(expected.equals(trimmed));
	}
	
}
