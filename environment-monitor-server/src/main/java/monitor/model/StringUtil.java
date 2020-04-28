package monitor.model;

public class StringUtil {

	public static final Object safeSubString(String string, int length) {
		if (string != null && string.length() > length) {
			return string.substring(0, length) + "...";
		} else {
			return string;
		}
	}	
	
}
