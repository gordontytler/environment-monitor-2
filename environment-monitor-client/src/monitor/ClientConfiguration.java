package monitor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ClientConfiguration {

	static Logger logger = Logger.getLogger(ClientConfiguration.class.getName());
	
	private static final String FILE = "config.properties";
	private static Properties properties = new Properties();	
	private static final ClientConfiguration theInstance = new ClientConfiguration();	
	
	private ClientConfiguration() {
		// try to load the properties and create if they don't exist
		if (!loadProperties()) {
			setDefaultProperties();
			saveDefaultProperties();
		}
	}

	public static ClientConfiguration getInstance() {
		return theInstance;
	}	
	
	private void setDefaultProperties() {
		properties.setProperty("MonitorURL", "http://localhost:8084/Monitor");
		properties.setProperty("ChunkedOutputArraySize", "200");
		properties.setProperty("LogFine", "false"); // true when testing this application, otherwise false. java.util.logging won't put FINE messages in the console so they have to be INFO		
	}
	
	static boolean loadProperties() {
		File file = new File(FILE);
		try {
			FileReader reader = new FileReader(file);			
			properties.load(reader);
			logger.log(Level.WARNING, "loaded client properties from " + file.getAbsolutePath());			
			return true;
		} catch (Exception e) {
			logger.log(Level.WARNING, "could not load client config.properties file", e);
			return false;
		}
	}

	static void saveDefaultProperties() {
		File file = new File(FILE);
		try {
			FileOutputStream out = new FileOutputStream(file);
			properties.store(out, "created from defaults");
		} catch (Exception e) {
			logger.log(Level.WARNING, "could not create client config.properties file from defaults", e);		
		}
	}

	/** example :  http://localhost:8084/Monitor */
	public String getMonitorURL() {
		return properties.getProperty("MonitorURL");
	}
	
	/** How many bars of history to show on PictureIcon */
	public int getChunkedOutputArraySize() {
		String integer = properties.getProperty("ChunkedOutputArraySize");
		return Integer.parseInt(integer);
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
	
}
