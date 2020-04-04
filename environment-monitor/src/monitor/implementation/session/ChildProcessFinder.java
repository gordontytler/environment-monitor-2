package monitor.implementation.session;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import monitor.model.Command;
import monitor.model.CommandResult;

public class ChildProcessFinder {
	
	static Logger logger = Logger.getLogger(ChildProcessFinder.class.getName());
	
	ArrayList<Integer> getChildren(int parentProcessId, Session controlSession) {
		// --ppid 	list processes whos parent is the bash process
		// -o pid 	output the process id for these child processes
		boolean stripEchoedCommandFromResponse = false;
		Command getChildrenCommand = new Command(String.format("ps --ppid %d -o pid | grep -v PID", parentProcessId), stripEchoedCommandFromResponse);
		getChildrenCommand.setMillisBeforeTimeout(4000);
		CommandResult getChildrenResult = controlSession.executeCommand(getChildrenCommand);
		String processList = getChildrenResult.getOutput();
		return processOutputToList(processList);
	}

	public ArrayList<Integer> processOutputToList(String processList) {
		String[] lines = processList.split("\n");
		ArrayList<Integer> children = new ArrayList<Integer>(lines.length);
		for (String line : lines) {
			if (line.length() > 0) {
				// ignore lines that dont have numbers e.g. [1]+ Done
				try {
					int nextGenerationParent = Integer.parseInt(line.trim());
					children.add(nextGenerationParent);
				} catch (NumberFormatException e) {
					logger.log(Level.WARNING, "Expected a process number but got \"" + line + "\"");
				}
			}
		}
		return children;
	}
	
}
