package monitor.implementation.shell;


import static org.junit.Assert.assertEquals;
import monitor.implementation.shell.BashExecuter;
import monitor.model.Command;

import org.junit.Test;

@SuppressWarnings("deprecation")
public class BashExecuterTest {
	
	
	@Test
	
	public void testTimeoutOutput() throws InterruptedException {
		BashExecuter bashExecuter = new BashExecuter();
		String expected0 = "expected-response-0";
		String expected1 = "expected-response-1";
		String expected2 = "expected-response-2";		
		
		Command takesTwoSeconds0 = new Command("sleep 2 ; echo " + expected0);
		Command takesTwoSeconds1 = new Command("sleep 2 ; echo " + expected1);
		Command takesTwoSeconds2 = new Command("sleep 2 ; echo " + expected2);		
		
		takesTwoSeconds0.setMillisBeforeTimeout(2500);
		String response = bashExecuter.executeCommand(takesTwoSeconds0).getOutput();
		assertEquals(expected0 + "\n", response);
		
		// the command takes longer than the timeout so BashExecuter gets no output
		takesTwoSeconds1.setMillisBeforeTimeout(1250);
		response = bashExecuter.executeCommand(takesTwoSeconds1).getOutput();
		assertEquals("", response);
		
		Thread.sleep(1250); // gives the previous sleep time to finish 
		
		// but if we wait we might expect the next command to get the previous output we missed followed by its own 
		takesTwoSeconds2.setMillisBeforeTimeout(2100);
		response = bashExecuter.executeCommand(takesTwoSeconds2).getOutput();
		assertEquals(expected0 + "\n" + expected1 + "\n" + expected2 + "\n", bashExecuter.getChunkedOutput().getAllChunks());
	}
	
}	
