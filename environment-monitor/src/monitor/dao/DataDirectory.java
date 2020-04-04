package monitor.dao;

public class DataDirectory {

	static boolean isTest = false;

	public static void setTest(boolean isTest) {
		DataDirectory.isTest = isTest;
	}

	public static String getDataDirectory() {
		if (isTest) {
			return "data-test";
		} else {
			return "data";
		}
	}
}
