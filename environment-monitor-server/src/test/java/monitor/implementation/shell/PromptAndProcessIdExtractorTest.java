package monitor.implementation.shell;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PromptAndProcessIdExtractorTest {

	PromptAndProcessIdExtractor extractor = new PromptAndProcessIdExtractor();	
	
	@Test
	public void extractWhenCommandEchoed() {
		String output = "echo $$\n$ 3791 \n$ ";
		String result = extractor.extractPromptAndProcessId(output, output.lastIndexOf('\n'));
		System.out.println(result);
		assertEquals("OK", result);
		assertEquals(3791, extractor.getBashProcessId());
		assertEquals("$ ", extractor.getPrompt());
	}
	
	@Test
	public void extractWhenCommandNotEchoed() {
		String output = "3791\n$ ";
		String result = extractor.extractPromptAndProcessId(output, output.lastIndexOf('\n'));
		System.out.println(result);
		assertEquals("OK", result);
		assertEquals(3791, extractor.getBashProcessId());
		assertEquals("$ ", extractor.getPrompt());
	}
	
}
