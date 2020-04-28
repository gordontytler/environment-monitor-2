package monitor.implementation.shell;

import monitor.model.OutputHistory;
import monitor.model.Severity;

public class HistorySeverityMarker {
	
	// The log4j words are FATAL, ERROR, WARN, INFO, DEBUG
	// FATAL is almost never used so lets make ERROR the red one. 
	// The java.util words are SEVERE, WARNING, CONFIG, INFO, FINE, FINER, FINEST
	// I happen to know that Exception is quite bad so will be ORANGE even when inside an INFO
	
	OutputHistory makeHistory(String chunk) {
		OutputHistory outputHistory = new OutputHistory();
		outputHistory.setBytes(chunk.length());
		if (chunk.lastIndexOf("ERROR") > -1 || chunk.lastIndexOf("FATAL") > -1 || chunk.lastIndexOf("SEVERE") > -1) {
			outputHistory.setSeverity(Severity.RED.ordinal());
		} else if (chunk.lastIndexOf("xception") > -1) {
			outputHistory.setSeverity(Severity.ORANGE.ordinal());
		} else if (chunk.lastIndexOf("WARN") > -1) {
			outputHistory.setSeverity(Severity.YELLOW.ordinal());
		} else if (chunk.lastIndexOf("INFO") > -1) {
			outputHistory.setSeverity(Severity.YELLOWY_GREEN.ordinal());
		} else {
			outputHistory.setSeverity(Severity.GREEN.ordinal());
		}
		int index = 0, lines = 0;
		while ((index > -1)) {
			index = chunk.indexOf('\n', index + 1);
			lines++;
		}
		outputHistory.setLines(lines);
		return outputHistory;
	}	

	public String toHistorySeverityMarkerValue(int severity) {
		switch (severity) {
		case 0 : return "unformated";
		case 1 : return "INFO";
		case 2 : return "WARN";
		case 3 : return "xception";
		case 4 : return "ERROR";
		default : return "GREEN";
		}
	}	
	
	
}
