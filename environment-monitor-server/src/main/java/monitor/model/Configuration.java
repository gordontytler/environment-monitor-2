package monitor.model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import monitor.dao.DataDirectory;
import monitor.implementation.MonitorRuntimeException;


/**
 * To add a new property define the default, delete the file and rerun the test. 
 */
@SuppressWarnings("serial")
public class Configuration {

	static Logger logger = Logger.getLogger(Configuration.class.getName());
    
	private static final String FILE = DataDirectory.getDataDirectory() + "config.properties";
	private static final Configuration theInstance = new Configuration();
	private static final Encrypter encrypter = new Encrypter();
	private String localHostName;

	private class SortedProperties extends Properties {
		@Override
		@SuppressWarnings("unchecked")
		public synchronized Enumeration keys() {
			Enumeration keysEnum = super.keys();
			Vector keyList = new Vector();
			while (keysEnum.hasMoreElements()) {
				keyList.add(keysEnum.nextElement());
			}
			Collections.sort(keyList);
			return keyList.elements();
		}

	}
	
	private static SortedProperties properties; 
	
	private Configuration() {
		properties = new SortedProperties();		
		// try to load the properties and create if they don't exist
		if (!loadProperties()) {
			setDefaultProperties();
			saveDefaultProperties();
		}
	}
	
	/**
	 * Deleting the file and running the tests will create all necessary properties with default values.<br>
	 * Optional properties are:
	 * <ul>
	 * <li>user.default If not set value will come from environment variable USER.
	 * <li>user.somemachine For example, user.gordon-netbook=gordon will cause Session.login for gordon-netbook to run ssh gordon:gordon-netbook
	 * e.g.  		user.node414.test.carrero.es=devops
	 * <li>DefaultHost for tests. If not set this is your machine name.
	 * </ul>
	 */
	private void setDefaultProperties() {
		// If a user is specified for the machine always try to log on as that user.
		// Otherwise if user.auto.tryFirst", "true" try to logon as auto and if you can't 
		// logon as user.default. Having logged on as user.default if user.auto.create", "true" create the auto
		// user and logon as auto.
		properties.setProperty("user.auto", "auto");
		properties.setProperty("user.auto.password", "iY7UkS05R8UHtwzbqxscBw==");
		properties.setProperty("user.auto.tryFirst", "true");
		properties.setProperty("user.auto.create", "true");
		properties.setProperty("user.default", "gordon");
		properties.setProperty("user.default.password", "iY7UkS05R8UHtwzbqxscBw=="); // to change this run EncrypterTest
		properties.setProperty("user.default.password.2", "ihtJmup0aRZ8VJkqQBT7KwHr1ewQ7U2e"); // to change this run EncrypterTest		
		properties.setProperty("user.gordon-netbook", "gordon");
		properties.setProperty("user.gordon-netbook.password", "iY7UkS05R8UHtwzbqxscBw==");
		properties.setProperty("user.gordon-netbook-2", "gordon");
		properties.setProperty("user.gordon-netbook.password", "iY7UkS05R8UHtwzbqxscBw==");		
		properties.setProperty("user.tytlergubuntu01", "tytlerg");
		properties.setProperty("user.tytlergubuntu01.password", "6paptXZlNI8VQU6Dc3/wSA==");
		properties.setProperty("user.tytlergubuntu01-2", "tytlerg");
		properties.setProperty("user.tytlergubuntu01.password", "6paptXZlNI8VQU6Dc3/wSA==");		
		//properties.setProperty("DefaultHostForTests", "some-remote-host"); // uncomment to make tests use a remote host		
		properties.setProperty("MaximumServerSessions", "20");
		properties.setProperty("DefaultCommandTimeoutMillis", "1000");
		properties.setProperty("ClosedSessionTestFrequencyMillis", "125000");
		properties.setProperty("TestCommandTimeoutMillis", "200");
		properties.setProperty("ChunkedOutputArraySize", "200");
		properties.setProperty("MonitorPort", "8084");
		properties.setProperty("UnusedMinutesBeforeClosingSession", "5");
		properties.setProperty("UnusedMinutesBeforeLogoutSession", "60");
		properties.setProperty("SessionLoginTimeoutSeconds", "1");
		properties.setProperty("ApplicationHeartbeatMillis", "32000"); 
		properties.setProperty("UnusedMillisBeforeCloseOfFinishedActionSession", "67000"); // used when a script does not send the close message to MonitorHttpProvider
		properties.setProperty("MinutesToWaitAfterFailedLogin", "10"); // wait before retrying to prevent thousands of exceptions
		properties.setProperty("MaximumTerminalsPerSSHConnection", "10"); /** see {@link TooManySessionsTest}  */
		properties.setProperty("FullPathToInstallDirectory", "/home/" + System.getenv().get("USER") + "/environment-monitor/environment-monitor");
		properties.setProperty("LogFine", "false"); // true when testing this application, otherwise false. java.util.logging won't put FINE messages in the console so they have to be INFO
		properties.setProperty("LogFine.InputFromSSHReader.filter", "false");
		properties.setProperty("action.already.running.kill", "true");
		properties.setProperty("action.already.running.run.another", "true");
	}
	
	
	static boolean loadProperties() {
		File file = new File(FILE);
		try {
			FileReader reader = new FileReader(file);			
			properties.load(reader);
			logger.log(Level.INFO, "loaded properties from " + file.getAbsolutePath());			
			return true;
		} catch (Exception e) {
			logger.log(Level.SEVERE, "could not load config.properties file", e);
			return false;
		}
	}

	static void saveDefaultProperties() {
		File file = new File(FILE);
		try {
			FileOutputStream out = new FileOutputStream(file);
			properties.store(out, "Created from defaults. To recreate delete this file and run monitor.model.ConfigurationTest");
		} catch (Exception e) {
			logger.log(Level.SEVERE, "could not create config.properties file from defaults", e);		
		}
	}
	
	
	public static Configuration getInstance() {
		return theInstance;
	}

	/** Normally called with a real host name and you get back the default user if there is no specific user for the host.
	 * Can also be called with the special values "default" or "auto". */
	public User getUser(String host) {
		String password = null;
		String otherPassword = null;
		String decryptedPassword = null;
		String decryptedOtherPassword = null;
		
		String name = (String) properties.get("user." + host);

		if (name != null) {
			password = (String) properties.get("user." + host + ".password");
			otherPassword = (String) properties.get("user." + host + ".password.2");
			decryptedPassword = encrypter.decrypt(password);
			if (otherPassword == null) {
				decryptedOtherPassword = decryptedPassword;
			} else {
				decryptedOtherPassword = encrypter.decrypt(otherPassword);
			}
			return User.getInstance(host, name, decryptedPassword, decryptedOtherPassword);
		} else {
			name = (String) properties.get("user.default");
			password = (String) properties.get("user.default.password");
			otherPassword = (String) properties.get("user.default.password.2");
			decryptedPassword = encrypter.decrypt(password);
			if (otherPassword == null) {
				decryptedOtherPassword = decryptedPassword;
			} else {
				decryptedOtherPassword = encrypter.decrypt(otherPassword);
			}
			
			if (name == null) {
				name = System.getenv().get("USER");   // alternatives LOGNAME , USERNAME
				// don't know the password for the user running this program
			}
			return User.getInstance(host, name, decryptedPassword, decryptedOtherPassword);
		}
	}
	
	public boolean isAutoTryFirst() {
		String trueOrFalse = properties.getProperty("user.auto.tryFirst");
		return "true".equals(trueOrFalse) ? true : false;
	}

	public boolean isAutoCreate() {
		String trueOrFalse = properties.getProperty("user.auto.create");
		return "true".equals(trueOrFalse) ? true : false;
	}	
	
	public int getMaximumServerSessions() {
		String max = properties.getProperty("MaximumServerSessions");
		return Integer.parseInt(max);
	}
	
	public int getSessionLoginTimeoutSeconds() {
		String max = properties.getProperty("SessionLoginTimeoutSeconds");
		return Integer.parseInt(max);
	}	

	public int getDefaultCommandTimeoutMillis() {
		String integer = properties.getProperty("DefaultCommandTimeoutMillis");
		return Integer.parseInt(integer);
	}

	public int getTestCommandTimeoutMillis() {
		String integer = properties.getProperty("TestCommandTimeoutMillis");
		return Integer.parseInt(integer);
	}

	public int getClosedSessionTestFrequencyMillis() {
		String integer = properties.getProperty("ClosedSessionTestFrequencyMillis");
		return Integer.parseInt(integer);
	}	
	
	// The host that tests use to run remote commands. Change this to make tests run commands on a remote host. 
	public String getDefaultHostForTests() {
		String defaultHost = properties.getProperty("DefaultHostForTests");
		if (defaultHost == null) {
			try {
				defaultHost = InetAddress.getLocalHost().getHostName();
			} catch (UnknownHostException e) {
				throw new MonitorRuntimeException(e);
			}
		}
		return defaultHost;
	}

	public String getLocalHostName() {
		if (localHostName == null) {
			try {
				localHostName = InetAddress.getLocalHost().getHostName();
			} catch (UnknownHostException e) {
				throw new MonitorRuntimeException(e);
			}
		}
		return localHostName;
	}
	
	public int getChunkedOutputArraySize() {
		String integer = properties.getProperty("ChunkedOutputArraySize");
		return Integer.parseInt(integer);
	}

	/** example :  this-machine-name:8084 */
	public String getMonitorHostAndPort() {
		return getLocalHostName() + ":" + properties.getProperty("MonitorPort");
	}

	public int getUnusedMinutesBeforeClosingSession() {
		String integer = properties.getProperty("UnusedMinutesBeforeClosingSession");
		return Integer.parseInt(integer);
	}

	public int getUnusedMinutesBeforeLogoutSession() {
		String integer = properties.getProperty("UnusedMinutesBeforeLogoutSession");
		return Integer.parseInt(integer);
	}

	public int getApplicationHeartbeatMillis() {
		String integer = properties.getProperty("ApplicationHeartbeatMillis");
		return Integer.parseInt(integer);
	}

	public long getUnusedMillisBeforeCloseOfFinishedActionSession() {
		String integer = properties.getProperty("UnusedMillisBeforeCloseOfFinishedActionSession");
		return Integer.parseInt(integer);
	}

	public int getMinutesToWaitAfterFailedLogin() {
		String integer = properties.getProperty("MinutesToWaitAfterFailedLogin");
		return Integer.parseInt(integer);
	}

	public int getMaximumTerminalsPerSSHConnection() {
		String integer = properties.getProperty("MaximumTerminalsPerSSHConnection");
		return Integer.parseInt(integer);
	}

	public String getFullPathToInstallDirectory() {
		return properties.getProperty("FullPathToInstallDirectory");
	}

	/** true when testing this application, otherwise false. 
	 * java.util.logging won't put FINE messages in the console so they have to be INFO 
	 * <p>
	 * e.g.
	 * <p>
	 * 
	 * if (Configuration.getInstance().isLogFine()) logger.info(...
	 **/
	public boolean isLogFine() {
		String trueOrFalse = properties.getProperty("LogFine");
		return "true".equals(trueOrFalse) ? true : false;
	}

	public boolean isLogFineSomething(String something) {
		String trueOrFalse = properties.getProperty("LogFine." + something);
		return "true".equals(trueOrFalse) ? true : false;
	}
	
	public void setLogFine(boolean logFine) {
		properties.setProperty("LogFine", logFine ? "true" : "false"); // true when testing this application, otherwise false. java.util.logging won't put FINE messages in the console so they have to be INFO
		properties.setProperty("LogFine.InputFromSSHReader.filter", "false");
	}
	
	/** @param something e.g. "InputFromSSHReader.filter"*/
	public void setLogFineSomething(String something, boolean logFine) {
		properties.setProperty("LogFine." + something, logFine ? "true" : "false");
	}

	public boolean isActionAlreadyRunningKill() {
		String trueOrFalse = properties.getProperty("action.already.running.kill");
		return "true".equals(trueOrFalse) ? true : false;
	}

	public boolean isActionAlreadyRunningRunAnother() {
		String trueOrFalse = properties.getProperty("action.already.running.run.another");
		return "true".equals(trueOrFalse) ? true : false;
	}
	
}
