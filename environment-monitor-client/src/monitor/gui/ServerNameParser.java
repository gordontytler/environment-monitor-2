package monitor.gui;

import java.util.ArrayList;
import java.util.List;

public class ServerNameParser {

	public List<String> parse(String serverName) {
		List<String> names = new ArrayList<String>();
		String[] parts = serverName.split("\t");
		if (parts.length == 1) {
			names.add(parts[0].trim());
		} else {
			for (String part : parts) {
				part = part.trim();
				if (isServerName(part)) {
					names.add(part);
				}
			}
		}
		return names;
	}

	/** return true for valid hostname RFC1034 if the string contains a "." and the other chars are not all numbers
	 * so soa-multi-app and 10.160.116.165 are not server names but hamdev321.aws.dev.ham.uk.betfair is. */
	private boolean isServerName(String part) {
		if (part.length() == 0) {
			return false;
		}
		boolean foundDot = false;
		boolean foundLowerCaseLetter = false;
		for (int x=0; x < part.length(); x++) {
			char c = part.charAt(x);
			if (c < '-' || c == '/' || c > 'z' || (c > '9' && c < 'a')) {
				return false;
			}
			if (c == '.') {
				if (x + 1 < part.length() && part.charAt(x + 1) == '.' ) {
					return false;
				}
				foundDot = true;
				continue;
			}
			if (c >= 'a' && c <= 'z') {
				foundLowerCaseLetter = true;
			}
		}
		if (foundDot && foundLowerCaseLetter) {
			return true;
		}
		return false;
	}

}
