package monitor.dao;

public class DataDirectory {

	static boolean isTest = false;

	public static void setTest(boolean isTest) {
		DataDirectory.isTest = isTest;
	}

	// TODO  fix project structure so that this isn't necessary or say why I did this
	// I don't want to use the classloader to read config files because I don't want the files in the jar.
	public static String getDataDirectory() {

		if (isTest) {
			return "./target/test-classes/";
		} else {
			return "./data/";
		}
	}
}
