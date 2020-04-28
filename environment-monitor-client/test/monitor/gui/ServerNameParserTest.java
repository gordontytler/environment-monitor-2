package monitor.gui;

import java.util.List;

import org.junit.Test;
import junit.framework.Assert;

public class ServerNameParserTest {
	
	ServerNameParser serverNameParser = new ServerNameParser();
	String typedIn = "some-computer-name";
	String pastedFromBrowser  = "\tsoa-multi-app \tollade332.carrero.es \t10.160.116.165 \t  " +
	  "\t  soa-multi-app \tollade332.carrero.es \t10.160.116.236 \t \tsoa-multi-app \tollade344.carrero.es \t";
	
	@Test
	public void testSimpleName() {
		List<String> names = serverNameParser.parse(typedIn);
		Assert.assertEquals(1, names.size());
	}
	
	@Test
	public void testPastedFromBrowser() {
		List<String> names = serverNameParser.parse(pastedFromBrowser);
		Assert.assertEquals("[ollade332.carrero.es, ollade332.carrero.es, ollade344.carrero.es]",
				names.toString());
	}
	
	@Test
	public void printChars() {
		for (int x=32; x < 255; x++) {
			System.out.println(x + " " + (char)x);
		}
	}
}
