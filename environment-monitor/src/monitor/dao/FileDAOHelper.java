package monitor.dao;

import java.io.BufferedReader;
import java.io.IOException;

import monitor.implementation.MonitorRuntimeException;

public class FileDAOHelper {

	static String toFileName(String humanName) {
		StringBuilder sb = new StringBuilder();
		String lowerCase = humanName.toLowerCase();
		boolean previousWasHyphen = true;
		for (byte b : lowerCase.getBytes()) {
			if ((b >= 'a' && b <= 'z') || (b >= '0' && b <= '9')) {
				sb.append((char)b);
				previousWasHyphen = false;	
			} else {
				if (!previousWasHyphen) {
					sb.append('-');
					previousWasHyphen = true;
				}
			}
		}
		return sb.toString();
	}

	/** @return the next non-blank uncommented line or null if end of file. */
	static String skipPastBlankLinesAndComments(BufferedReader reader, LineNumber lineNumber) {
		String line = "";
		String trimmed = "";
		while (line != null && (trimmed.length() == 0 || trimmed.startsWith("#") || trimmed.startsWith("//"))) {
			try {
				line = reader.readLine();
			} catch (IOException e) {
				throw new MonitorRuntimeException(e);
			}
			if (line == null) {
				return null;
			}
			trimmed = line.trim();
			lineNumber.increment();
		}
		return trimmed;
	}	
	
}
