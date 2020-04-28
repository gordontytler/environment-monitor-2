package monitor.implementation.shell;

import java.util.logging.Level;
import java.util.logging.Logger;

import monitor.implementation.MonitorRuntimeException;
import monitor.model.Configuration;
import monitor.model.OutputHistory;

/**
 * Stores the previous arraySize chunks of output from the bash shell.  No threads should read while items
 * are being added but at other times multiple can read at the same time. Not sure how to enforce this.
 * <p>
 * Also, it turns out that the logger in combination with the synchronized methods here can cause nasty problems.
 */
public class ChunkedOutput {
	
	static Logger logger = Logger.getLogger(ChunkedOutput.class.getName());
	static final boolean logFine = Configuration.getInstance().isLogFine();
	
	private HistorySeverityMarker historySeverityMarker = new HistorySeverityMarker();
	
	private int arraySize;
	private String[] chunks;
	private long[] timeStamps;
	private OutputHistory[] histories;
	private int writePosition = 0;	
	private int highestChunkNumber = -1;
	private int lowestChunkNumber = -1;

	public ChunkedOutput(int arraySize) {
		this.arraySize = arraySize;
		chunks = new String[arraySize];
		timeStamps = new long[arraySize];
		histories = new OutputHistory[arraySize]; 
	}
	
	synchronized void append(String chunk) {
		chunks[writePosition] = chunk;
		timeStamps[writePosition] = System.nanoTime();
		histories[writePosition] = historySeverityMarker.makeHistory(chunk);

		writePosition++;
		highestChunkNumber++;

		if (writePosition > arraySize - 1) {
			writePosition = 0;
		}
		
		if (highestChunkNumber > arraySize -1) {
			lowestChunkNumber++;			
		} else {
			lowestChunkNumber = 0;
		}
	}

	public synchronized String getChunk(int chunkNumber) {
		if (lowestChunkNumber == -1) {
			return "";
		}
		int index = calculateIndex(chunkNumber);
		try {
			return chunks[index];
		} catch (Exception e) {
			logger.log(Level.SEVERE, "wrong index calculation for chunks", e);
			return "";
		}
	}

	public synchronized long getTimeStamp(int chunkNumber) {
		if (lowestChunkNumber == -1) {
			return Long.MAX_VALUE;
		}
		
		int index = calculateIndex(chunkNumber);
		try {
			return timeStamps[index];
		} catch (Exception e) {
			logger.log(Level.SEVERE, "wrong index calculation for timeStamps", e);
			return Long.MAX_VALUE;			
		}		
	}
	
	public OutputHistory getOutputHistory(int chunkNumber) {
		if (lowestChunkNumber == -1) {
			return new OutputHistory();
		}
		int index = calculateIndex(chunkNumber);
		try {
			return histories[index];
		} catch (Exception e) {
			logger.log(Level.SEVERE, "wrong index calculation for histories", e);
			return new OutputHistory();
		}			
	}
	
	private int calculateIndex(int chunkNumber) {
		if (chunkNumber > highestChunkNumber) {
			if (logFine) logger.log(Level.INFO, String.format("requested chunkNumber %d is greater than highest available: %d", chunkNumber, highestChunkNumber));
			chunkNumber = highestChunkNumber;
		}		
		if (chunkNumber < lowestChunkNumber) {
			if (logFine) logger.log(Level.INFO, String.format("requested chunkNumber %d is before the oldest stored: %d", chunkNumber, lowestChunkNumber));
			chunkNumber = lowestChunkNumber;
		}		
		
		int index = chunkNumber;
		
		if (highestChunkNumber > arraySize -1) {
			if (chunkNumber > highestChunkNumber - writePosition) {
				index = writePosition - (highestChunkNumber - chunkNumber) - 1;				
			} else {
				index = writePosition + (chunkNumber - lowestChunkNumber);
			}
			
		}
		return index;
	}

	public int getHighestChunkNumber() {
		return highestChunkNumber;
	}

	public int getLowestChunkNumber() {
		return lowestChunkNumber;
	}
	
	public String getAllChunks() {
		return getChunks(lowestChunkNumber, highestChunkNumber);
	}

	public String getChunks(int from, int to) {
		if (to - from > arraySize) {
			String message = String.format("chunks requested is greater than array size: to:%d - from:%d > arraySize:%d", to, from, arraySize);
			Exception e = new MonitorRuntimeException(message);
			e.fillInStackTrace();
			logger.log(Level.SEVERE, message, e);
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (int x = from; from > -1 && x <= to; x++) {
			sb.append(getChunk(x));
			Thread.yield();
		}
		return sb.toString();
	}
	

	/** Used by Discover All Apps. When the session is closed a new ChunkedOutput is created. */
	public void setNonStandardArraySize(int arraySize) {
		this.arraySize = arraySize;
		chunks = new String[arraySize];
		timeStamps = new long[arraySize];
		histories = new OutputHistory[arraySize]; 
	}
	
	
	public void setHistorySeverityMarker(HistorySeverityMarker historySeverityMarker) {
		this.historySeverityMarker = historySeverityMarker;
	}
	
}
