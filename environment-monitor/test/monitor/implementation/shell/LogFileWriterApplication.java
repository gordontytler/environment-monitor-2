package monitor.implementation.shell;

import java.io.FileNotFoundException;
import java.io.IOException;

public class LogFileWriterApplication {

	/**
	 * If run from Eclipse start log file writers for files hard coded here.
	 * Otherwise, use write to sever.log in -Dlog.dir=/var/log/some-application
	 * scripts/discover-apps-001.py will think this is a real application.
	 * @param args
	 * @throws InterruptedException 
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException {
		LogFileGenerator generator = new LogFileGenerator();
		String logDir = System.getProperty("log.dir"); 
		if  (logDir != null) {
			while (true) {
				generator.writeLog(logDir + "/server.log");	
			}
			
		} else {
			boolean generate = false;
			
			while (true && generate) {
				generator.writeLog("/var/log/decision-maker/server.log");
			}
			
			// do this when running in Eclipse
			LogFileReplayer writer = new LogFileReplayer();
			while (true) {
				writer.replay("/var/log/decision-maker/server.log", "testLogOutput.txt", 1);
			}
		}

	}

}
