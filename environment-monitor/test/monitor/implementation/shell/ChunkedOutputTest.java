package monitor.implementation.shell;

import static org.junit.Assert.assertEquals;

import java.util.logging.Logger;

import org.junit.Test;

public class ChunkedOutputTest {

	static Logger logger = Logger.getLogger(ChunkedOutputTest.class.getName());
	
	@Test
	public void testAppend() {
		
		ChunkedOutput o = new ChunkedOutput(10);
		
		o.append("0");
		assertEquals(new Integer(0).toString(), o.getChunk(0));
		
		String stringX;
		for (int x=1; x<10; x++  ) {
			stringX = new Integer(x).toString();
			o.append(stringX);
			assertEquals(stringX, o.getChunk(x));
		}

		// we only have room for 10 chunks and already have 0..9
		
		stringX = new Integer(10).toString();
		o.append(stringX);
		assertEquals(stringX, o.getChunk(10));
		
	}

	

	@Test
	public void testBeforeWritePosition() {
		
		ChunkedOutput o = new ChunkedOutput(10);
		
		String stringX;
		for (int x=0; x<13; x++) {
			stringX = new Integer(x).toString();
			o.append(stringX);
			assertEquals(stringX, o.getChunk(x));
		}
		assertEquals("10", o.getChunk(10)); // element 0
		assertEquals("11", o.getChunk(11));	// element 1
		assertEquals("12", o.getChunk(12));	// element 2
		assertEquals("3", o.getChunk(3));	// element 3
		assertEquals("4", o.getChunk(4));	// element 4
		assertEquals("9", o.getChunk(9));	// element 9		

		assertEquals("3", o.getChunk(2));	// 3 is the oldest		
		assertEquals("12", o.getChunk(13));	// 12 is the newest		
	}

	

	@Test
	public void testTwoLoops() {
		
		ChunkedOutput o = new ChunkedOutput(10);
		
		String stringX;
		for (int x=0; x<24; x++) {
			stringX = new Integer(x).toString();
			o.append(stringX);
			assertEquals(stringX, o.getChunk(x));
		}
		assertEquals(23, o.getHighestChunkNumber()); // element 3
		assertEquals(14, o.getLowestChunkNumber()); // element 9		
		
		assertEquals("20", o.getChunk(20)); // element 0
		assertEquals("21", o.getChunk(21));	// element 1
		assertEquals("22", o.getChunk(22));	// element 2
		assertEquals("23", o.getChunk(23));	// element 3
		assertEquals("14", o.getChunk(14));	// element 4
		assertEquals("19", o.getChunk(19));	// element 9		

		
	}

	
	@Test
	public void testEmptyChunkedOutput() {
		ChunkedOutput o = new ChunkedOutput(10);
		assertEquals("", o.getChunk(-1));		
		assertEquals("", o.getChunk(20));
		assertEquals("", o.getChunk(-2));		
	}
}
