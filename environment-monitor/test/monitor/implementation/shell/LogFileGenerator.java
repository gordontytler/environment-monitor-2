package monitor.implementation.shell;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Logger;

public class LogFileGenerator {
	
	static Logger logger = Logger.getLogger(LogFileGenerator.class.getName());	
	private HistorySeverityMarker historySeverityMarker = new HistorySeverityMarker();
	
	public void writeLog(String outputFileName) throws IOException, InterruptedException {
		File outputFile = new File(outputFileName);
		
		if (!outputFile.exists()) {
			String directories = outputFileName.substring(0, outputFileName.lastIndexOf(File.separatorChar));
			File dirs = new File(directories);
			if (!dirs.exists()) {
				if (!dirs.mkdirs()) {
					throw new IOException("Could not create directories: " + directories);
				}
			}
			outputFile.createNewFile();
		}

		PrintWriter p = new PrintWriter(new BufferedWriter(new FileWriter(outputFile)));
		
		// random
		for (int x=0; x<50; x++) {
			int bytes = (int)(Math.random() * 1000) + 1;
			int lines = (int)(Math.random() * 30) + 1;
			int severity = (int)(Math.random() * 5);
			writeLines(p, bytes, lines, severity);
			Thread.sleep(1000);
		}
		p.close();
	}
	
	private void writeLines(PrintWriter p, int bytes, int lines, int severity) {
		int bytesPerLine = bytes / lines;
		char[] chars = new char[bytesPerLine];
		Arrays.fill(chars, 'X');
		String line = new String(chars);
		String dateStamp = new SimpleDateFormat(LogFileReplayer.LOG_FORMAT).format(new Date());
		p.println(dateStamp + " " + historySeverityMarker.toHistorySeverityMarkerValue(severity) + " " + line);
		for (int l=0; l < lines -1; l++) {
			p.println(line);	
		}
		p.flush();
		//logger.info(String.format("wrote %d bytes %d line of severity %s", bytes, lines, Severity.values()[severity] ));
	}

	
}
