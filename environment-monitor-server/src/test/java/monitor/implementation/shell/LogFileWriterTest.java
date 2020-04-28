package monitor.implementation.shell;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import monitor.dao.DataDirectory;
import monitor.dao.EnvironmentViewDAO;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class LogFileWriterTest {
	@Before
	public void before() {
		DataDirectory.setTest(true);
	}
	
	@Test
	public void testfileLogMillis() {
		LogFileReplayer writer = new LogFileReplayer();
		Calendar now = Calendar.getInstance();
		long millisSoFarToday = 
			( now.get(Calendar.HOUR_OF_DAY) * 60 * 60 * 1000 ) +
			( now.get(Calendar.MINUTE) * 60 * 1000 ) + 
			( now.get(Calendar.SECOND) * 1000 ) +
			now.get(Calendar.MILLISECOND);
		System.out.println(String.format("%s-%s-%s-%s", now.get(Calendar.HOUR_OF_DAY),  now.get(Calendar.MINUTE), now.get(Calendar.SECOND),  now.get(Calendar.MILLISECOND)));
		Assert.assertEquals(writer.fileLogMillis("00:00:00,000, ERROR blah blah"), now.getTimeInMillis() - millisSoFarToday);
	}

	@Test
	public void testToMillisAndBack() {
		LogFileReplayer writer = new LogFileReplayer();
		long millis = writer.fileLogMillis("02:10:20,808, ERROR blah blah");
		// having converted the log file date into millis convert it back
		SimpleDateFormat format = new SimpleDateFormat(LogFileReplayer.YYYYMMDD + LogFileReplayer.LOG_FORMAT);
		String formatted = format.format(new Date(millis));
		Calendar now = Calendar.getInstance();
		Assert.assertEquals(String.format("%s-%02d-%02d-", now.get(Calendar.YEAR), (now.get(Calendar.MONTH) + 1), now.get(Calendar.DATE)) + "02:10:20,808", 
				formatted);
	}
	
	@Test 
	public void testMillisInFile() throws FileNotFoundException, IOException {
		LogFileReplayer writer = new LogFileReplayer();
		List<String> lines = writer.load("testLogOutput.txt");
		long[] millis = new long[5];
		for (int x=0; x < 5; x++) {
			millis[x] = writer.fileLogMillis(lines.get(x + 1));
		}
		Assert.assertEquals(11, millis[1] - millis[0]);  //   17:05:23,305  -  17:05:23,294
		Assert.assertEquals(2, millis[2] - millis[1]);   //   17:05:23,307  -  17:05:23,305
		Assert.assertEquals(0, millis[3] - millis[2]);   //   17:05:23,307  -  17:05:23,307
		Assert.assertEquals(2, millis[4] - millis[3]);	 //   17:05:23,309  -  17:05:23,307
	}
	
}
