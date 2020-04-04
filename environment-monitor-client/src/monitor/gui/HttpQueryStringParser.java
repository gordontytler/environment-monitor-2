package monitor.gui;

import java.util.HashMap;
import java.util.Map;

/**
 * This is a copy of the identical class in package monitor.api
 * The client uses it application menu data from EnvironmentMouseAdapter to EnvironmentScrollPane.
 */

public class HttpQueryStringParser {

	private Map<String, String> params;
	
	public HttpQueryStringParser(String queryString) {
		this.params = parse(queryString);
	}

	/**
	 * @param http get queryString with ? removed e.g. name1=value1&name2=value2&name3=value3
	 * <p>The input should have already been decoded using {@link java.net.URLDecoder#decode(String, String)}
	 * @return map of name value pairs
	 */
	private Map<String, String> parse(String queryString) {
		Map<String, String> result = new HashMap<String, String>();
		for (String pair : queryString.split("&")) {
			String[] parts = pair.split("=");
			if (parts.length > 0) {
				result.put(parts[0], parts[1]);
			}
		}
		return result;
	}

	public String missingRequiredParameters(String[] strings) {
		StringBuilder sb = new StringBuilder();
		for (String arg : strings) {
			if (!params.containsKey(arg)) {
				if (sb.length()>0) {
					sb.append(", ");
				}
				sb.append(arg);
			}
		}
		return sb.toString();
	}
	
	public String getParameterValue(String name) {
		return params.get(name);
	}
	
}
