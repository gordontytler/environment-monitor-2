package monitor.implementation.shell;

import static org.junit.Assert.assertEquals;
import monitor.model.OutputHistory;

import org.junit.Test;



public class OutputHistoryFactoryTest {

	@Test
	public void testLinesBytesSeverity() {
		String chunk = 
			"215.004: [GC 215.004: [DefNew: 104960K->0K(118016K), 0.0491790 secs] 183179K->81917K(908544K), 0.0493530 secs] [Times: user=0.05 sys=0.00, real=0.05 secs]\n" + 
			"17:08:54,761, ERROR [STDERR] log4j:ERROR Could not create an Appender. Reported error follows.\n" +
			"17:08:54,761, ERROR [STDERR] java.lang.ClassCastException: com.carrero.logging.appender.CentralSyslogAppender cannot be cast to org.apache.log4j.Appender\n" +
			"17:08:54,762, ERROR [STDERR]    at org.apache.log4j.xml.DOMConfigurator.parseAppender(DOMConfigurator.java:175)\n" +
			"17:08:54,762, ERROR [STDERR]    at org.apache.log4j.xml.DOMConfigurator.findAppenderByName(DOMConfigurator.java:150)\n" +
			"17:08:54,762, ERROR [STDERR]    at org.apache.log4j.xml.DOMConfigurator.findAppenderByReference(DOMConfigurator.java:163)\n" +
			"17:08:54,763, ERROR [STDERR]    at org.apache.log4j.xml.DOMConfigurator.parseChildrenOfLoggerElement(DOMConfigurator.java:425)\n" +
			"17:08:54,763, ERROR [STDERR]    at org.apache.log4j.xml.DOMConfigurator.parseCategory(DOMConfigurator.java:345)";
		OutputHistory history = new HistorySeverityMarker().makeHistory(chunk);
		String expected = "OutputHistory [bytes=993, lines=8, severity=4]";
		String actual = history.toString();
		assertEquals(expected, actual);
	}
	
}
