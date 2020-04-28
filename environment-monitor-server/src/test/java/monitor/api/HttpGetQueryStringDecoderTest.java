package monitor.api;

import org.junit.Assert;
import org.junit.Test;

public class HttpGetQueryStringDecoderTest {
	
	@Test
	public void parse() {
		HttpQueryStringParser parser = new HttpQueryStringParser("nameInEnvironmentView=test added application&sessionId=9&fileName=applications/test-added-application-001.txt");
		Assert.assertEquals("test added application", parser.getParameterValue("nameInEnvironmentView"));
		Assert.assertEquals("9", parser.getParameterValue("sessionId"));
		Assert.assertEquals("applications/test-added-application-001.txt", parser.getParameterValue("fileName"));
		Assert.assertEquals("missing, another", parser.missingRequiredParameters(
				new String[]{"nameInEnvironmentView","missing","sessionId","fileName","another"} ));
	}
	
}
